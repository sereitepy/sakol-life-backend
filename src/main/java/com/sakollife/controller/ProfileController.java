package com.sakollife.controller;

import com.sakollife.entity.Profile;
import com.sakollife.entity.QuizAttempt;
import com.sakollife.repository.ProfileRepository;
import com.sakollife.repository.QuizAnswerRepository;
import com.sakollife.repository.QuizAttemptRepository;
import com.sakollife.service.impl.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileRepository profileRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizService quizService;

    /**
     * POST /api/v1/profile/init
     *
     * Creates the profile row for a newly registered user.
     */
    @PostMapping("/init")
    public ResponseEntity<?> initProfile(
            @RequestBody Map<String, String> body,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        if (profileRepository.existsById(userId)) {
            return ResponseEntity.ok(profileRepository.findById(userId).get());
        }

        Profile profile = Profile.builder()
                .id(userId)
                .displayName(body.getOrDefault("displayName", "New User"))
                .preferredLanguage(
                        body.containsKey("preferredLanguage")
                                ? com.sakollife.entity.enums.Language.valueOf(body.get("preferredLanguage"))
                                : com.sakollife.entity.enums.Language.EN
                )
                .role(com.sakollife.entity.enums.Role.USER)
                .build();

        profileRepository.save(profile);
        return ResponseEntity.status(201).body(Map.of(
                "message", "Profile created successfully",
                "id", userId,
                "displayName", profile.getDisplayName()
        ));
    }

    /**
     * GET /api/v1/profile
     *
     * Returns the authenticated user's full profile including:
     * - display name, picture, language preference
     * - total quiz attempt count
     * - latest quiz answers (for re-populating the quiz review)
     * - currently selected major (null if none — determines University tab availability)
     */
    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        long attemptCount = quizService.getAttemptCount(userId);

        Optional<QuizAttempt> latestAttempt = quizAttemptRepository.findLatestByUserId(userId);
        List<Map<String, String>> latestAnswers = latestAttempt
                .map(attempt -> quizAnswerRepository
                        .findByAttemptIdOrderByQuestionCodeAsc(attempt.getId())
                        .stream()
                        .map(a -> Map.of(
                                "questionCode", a.getQuestionCode(),
                                "answerValue",  a.getAnswerValue()))
                        .toList())
                .orElse(Collections.emptyList());

        // Build selected major block . null-safe
        Map<String, Object> selectedMajorBlock = null;
        if (profile.getSelectedMajor() != null) {
            var m = profile.getSelectedMajor();
            selectedMajorBlock = new LinkedHashMap<>();
            selectedMajorBlock.put("majorId",       m.getId());
            selectedMajorBlock.put("code",          m.getCode());
            selectedMajorBlock.put("nameEn",        m.getNameEn());
            selectedMajorBlock.put("nameKh",        m.getNameKh());
            selectedMajorBlock.put("descriptionEn", m.getDescriptionEn());
            selectedMajorBlock.put("descriptionKh", m.getDescriptionKh());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id",                 profile.getId());
        response.put("displayName",        profile.getDisplayName());
        response.put("profilePictureUrl",  profile.getProfilePictureUrl() != null ? profile.getProfilePictureUrl() : "");
        response.put("preferredLanguage",  profile.getPreferredLanguage());
        response.put("role",               profile.getRole());
        response.put("totalAttempts",      attemptCount);
        response.put("selectedMajor",      selectedMajorBlock);   // null = university tab disabled
        response.put("latestAnswers",       latestAnswers);

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/profile
     *
     * Update profile fields.
     * Note: selectedMajor is NOT updated here.
     * Use PUT /api/v1/selected-major and DELETE /api/v1/selected-major instead.
     */
    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, String> updates,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        if (updates.containsKey("displayName")) {
            profile.setDisplayName(updates.get("displayName"));
        }
        if (updates.containsKey("profilePictureUrl")) {
            profile.setProfilePictureUrl(updates.get("profilePictureUrl"));
        }
        if (updates.containsKey("preferredLanguage")) {
            profile.setPreferredLanguage(
                    com.sakollife.entity.enums.Language.valueOf(updates.get("preferredLanguage")));
        }

        profileRepository.save(profile);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}
