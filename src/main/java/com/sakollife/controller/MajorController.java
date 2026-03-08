package com.sakollife.controller;

import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.sakollife.entity.GuestMajorSelection;
import com.sakollife.entity.Major;
import com.sakollife.entity.Profile;
import com.sakollife.entity.SavedMajor;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorRepository majorRepository;
    private final QuizResultRepository quizResultRepository;
    private final SavedMajorRepository savedMajorRepository;
    private final GuestMajorSelectionRepository guestMajorSelectionRepository;
    private final UniversityMajorRepository universityMajorRepository;
    private final ProfileRepository profileRepository;

    @GetMapping
    public ResponseEntity<?> getAllMajors() {
        return ResponseEntity.ok(majorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMajor(@PathVariable UUID id) {
        return majorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/results/{attemptId}")
    public ResponseEntity<?> getResults(@PathVariable UUID attemptId) {
        var results = quizResultRepository.findQualifyingResultsByAttemptId(attemptId);
        if (results.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(results.stream().map(r -> {
            var m = r.getMajor();
            return Map.of(
                    "majorId", m.getId(),
                    "code", m.getCode(),
                    "nameEn", m.getNameEn(),
                    "nameKh", m.getNameKh(),
                    "descriptionEn", m.getDescriptionEn(),
                    "descriptionKh", m.getDescriptionKh(),
                    "rank", r.getRank(),
                    "similarityScore", r.getSimilarityScore(),
                    "similarityPercentage", (int) Math.round(r.getSimilarityScore().doubleValue() * 100)
            );
        }).toList());
    }

    /**
     * POST /api/v1/majors/{majorId}/select
     * Response: the university list for the selected major
     */
    @PostMapping("/{majorId}/select")
    @Transactional
    public ResponseEntity<?> selectMajor(
            @PathVariable UUID majorId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NoSuchElementException("Major not found"));

        if (userId != null) {
            if (!savedMajorRepository.existsByUserIdAndMajorId(userId, majorId)) {
                Profile profile = profileRepository.getReferenceById(userId);
                savedMajorRepository.save(
                        SavedMajor.builder().user(profile).major(major).build()
                );
            }
        } else if (guestSessionId != null) {
            guestMajorSelectionRepository.deleteByGuestSessionId(guestSessionId);
            guestMajorSelectionRepository.save(
                    GuestMajorSelection.builder()
                            .guestSessionId(guestSessionId)
                            .major(major)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either X-User-Id or X-Guest-Session-Id header"));
        }

        // Return university list immediately
        List<Map<String, Object>> universities = universityMajorRepository
                .findByMajorWithFilters(majorId, null, null, null, null)
                .stream()
                .map(um -> {
                    var u = um.getUniversity();
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("universityMajorId", um.getId());
                    map.put("universityId", u.getId());
                    map.put("nameEn", u.getNameEn());
                    map.put("nameKh", u.getNameKh());
                    map.put("type", u.getType());
                    map.put("locationCity", u.getLocationCity());
                    map.put("tuitionFeeUsd", um.getTuitionFeeUsd());
                    map.put("durationYears", um.getDurationYears());
                    return map;
                }).toList();

        return ResponseEntity.ok(Map.of(
                "selectedMajor", Map.of(
                        "id", major.getId(),
                        "code", major.getCode(),
                        "nameEn", major.getNameEn(),
                        "nameKh", major.getNameKh()
                ),
                "universities", universities
        ));
    }

    /**
     * GET /api/v1/majors/selected
     * Returns the currently selected major for a user or guest.
     */
    @GetMapping("/selected")
    public ResponseEntity<?> getSelected(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        if (userId != null) {
            var saved = savedMajorRepository.findByUserIdOrderBySavedAtDesc(userId);
            return ResponseEntity.ok(saved.stream().map(s -> Map.of(
                    "majorId", s.getMajor().getId(),
                    "code", s.getMajor().getCode(),
                    "nameEn", s.getMajor().getNameEn(),
                    "savedAt", s.getSavedAt()
            )).toList());
        } else if (guestSessionId != null) {
            return guestMajorSelectionRepository.findByGuestSessionId(guestSessionId)
                    .map(s -> ResponseEntity.ok((Object) Map.of(
                            "majorId", s.getMajor().getId(),
                            "code", s.getMajor().getCode(),
                            "nameEn", s.getMajor().getNameEn(),
                            "selectedAt", s.getSelectedAt()
                    )))
                    .orElse(ResponseEntity.notFound().build());
        }

        return ResponseEntity.badRequest()
                .body(Map.of("error", "Provide either X-User-Id or X-Guest-Session-Id header"));
    }
}