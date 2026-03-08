package com.sakollife.controller.admin;

import com.sakollife.entity.Profile;
import com.sakollife.entity.QuizAttempt;
import com.sakollife.entity.QuizResult;
import com.sakollife.repository.ProfileRepository;
import com.sakollife.repository.QuizAttemptRepository;
import com.sakollife.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final ProfileRepository profileRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizResultRepository quizResultRepository;

    /**
     * GET /api/v1/admin/users?page=0&size=20
     * Paginated list of all registered users.
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Profile> users = profileRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", users.getTotalElements());
        response.put("totalPages", users.getTotalPages());
        response.put("currentPage", page);
        response.put("users", users.getContent().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("displayName", u.getDisplayName());
            m.put("preferredLanguage", u.getPreferredLanguage());
            m.put("role", u.getRole());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/admin/users/{userId}
     * Get a single user's profile.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable UUID userId) {
        return profileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/admin/users/{userId}/quiz-history
     * Get all quiz attempts for a specific user, with their top results.
     */
    @GetMapping("/{userId}/quiz-history")
    public ResponseEntity<?> getUserQuizHistory(@PathVariable UUID userId) {
        List<QuizAttempt> attempts = quizAttemptRepository
                .findByUserIdOrderByAttemptNumberDesc(userId);

        List<Map<String, Object>> history = attempts.stream().map(attempt -> {
            // Get top 3 results for this attempt for the summary view
            List<QuizResult> topResults = quizResultRepository
                    .findByAttemptIdOrderByRankAsc(attempt.getId())
                    .stream().limit(3).toList();

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("attemptId", attempt.getId());
            entry.put("attemptNumber", attempt.getAttemptNumber());
            entry.put("takenAt", attempt.getCreatedAt());
            entry.put("studentVector", new double[]{
                    attempt.getVecR().doubleValue(),
                    attempt.getVecI().doubleValue(),
                    attempt.getVecA().doubleValue(),
                    attempt.getVecS().doubleValue(),
                    attempt.getVecE().doubleValue(),
                    attempt.getVecC().doubleValue()
            });
            entry.put("topMatches", topResults.stream().map(r -> {
                Map<String, Object> match = new LinkedHashMap<>();
                match.put("rank", r.getRank());
                match.put("majorCode", r.getMajor().getCode());
                match.put("majorNameEn", r.getMajor().getNameEn());
                match.put("similarityPercentage", (int) Math.round(r.getSimilarityScore().doubleValue() * 100));
                return match;
            }).toList());

            return entry;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "totalAttempts", attempts.size(),
                "history", history
        ));
    }

    /**
     * PUT /api/v1/admin/users/{userId}/role
     * Promote or demote a user (USER <-> ADMIN).
     * Body: { "role": "ADMIN" }
     */
    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable UUID userId,
                                             @RequestBody Map<String, String> body) {
        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        profile.setRole(com.sakollife.entity.enums.Role.valueOf(body.get("role")));
        profileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
                "message", "Role updated",
                "userId", userId,
                "newRole", profile.getRole()
        ));
    }
}
