package com.sakollife.controller;

import com.sakollife.entity.Major;
import com.sakollife.entity.Profile;
import com.sakollife.repository.MajorRepository;
import com.sakollife.repository.ProfileRepository;
import com.sakollife.repository.UniversityMajorRepository;
import com.sakollife.entity.enums.UniversityType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/selected-major")
@RequiredArgsConstructor
public class SelectedMajorController {

    private final ProfileRepository profileRepository;
    private final MajorRepository majorRepository;
    private final UniversityMajorRepository universityMajorRepository;

    /**
     * GET /api/v1/selected-major
     *
     * Returns the user's currently selected major, or null if none selected.
     * Frontend uses this on dashboard load to know whether to show the University tab.
     *
     * Response when major IS selected:
     * {
     *   "selected": true,
     *   "major": {
     *     "majorId": "uuid",
     *     "code": "AI",
     *     "nameEn": "Artificial Intelligence",
     *     "nameKh": "...",
     *     "descriptionEn": "...",
     *     "descriptionKh": "..."
     *   }
     * }
     *
     * Response when NO major is selected:
     * {
     *   "selected": false,
     *   "major": null
     * }
     */
    @GetMapping
    public ResponseEntity<?> getSelectedMajor(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        if (profile.getSelectedMajor() == null) {
            return ResponseEntity.ok(Map.of("selected", false, "major", Collections.emptyMap()));
        }

        Major m = profile.getSelectedMajor();
        return ResponseEntity.ok(Map.of(
                "selected", true,
                "major", buildMajorMap(m)
        ));
    }

    /**
     * PUT /api/v1/selected-major
     *
     * Select a major. Replaces any previously selected major (only 1 allowed at a time).
     * Called when the user clicks a major card on the results page.
     *
     * Body:
     * { "majorId": "uuid-of-the-major" }
     *
     * Response (200 OK):
     * {
     *   "selected": true,
     *   "major": { ... major fields ... }
     * }
     */
    @PutMapping
    public ResponseEntity<?> selectMajor(@RequestBody Map<String, String> body,
                                          Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        UUID majorId = UUID.fromString(body.get("majorId"));

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NoSuchElementException("Major not found: " + majorId));

        profile.setSelectedMajor(major);
        profileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
                "selected", true,
                "major", buildMajorMap(major)
        ));
    }

    /**
     * DELETE /api/v1/selected-major
     *
     * Deselect the current major (user clicked X).
     * After this call, the University tab becomes unavailable on the frontend.
     *
     * No body required.
     *
     * Response (200 OK):
     * { "selected": false, "message": "Major deselected" }
     */
    @DeleteMapping
    public ResponseEntity<?> deselectMajor(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        profile.setSelectedMajor(null);
        profileRepository.save(profile);

        return ResponseEntity.ok(Map.of(
                "selected", false,
                "message", "Major deselected"
        ));
    }

    /**
     * GET /api/v1/selected-major/universities
     *
     * Convenience endpoint. returns the universities for the user's CURRENTLY selected major.
     * Avoids the frontend needing to make 2 calls (get selected major → get universities).
     * Supports the same optional filters as GET /api/v1/universities.
     *
     * Returns 409 if no major is currently selected (frontend should hide this tab anyway).
     */
    @GetMapping("/universities")
    public ResponseEntity<?> getUniversitiesForSelectedMajor(
            Authentication authentication,
            @RequestParam(required = false) UniversityType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) Integer durationYears) {

        UUID userId = (UUID) authentication.getPrincipal();

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Profile not found"));

        if (profile.getSelectedMajor() == null) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "No major selected",
                    "message", "Select a major first before viewing universities"
            ));
        }

        UUID majorId = profile.getSelectedMajor().getId();

        var results = universityMajorRepository.findByMajorWithFilters(
                majorId, type, city, maxFee, durationYears);

        var response = results.stream().map(um -> {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("universityId",   um.getUniversity().getId());
            entry.put("nameEn",         um.getUniversity().getNameEn());
            entry.put("nameKh",         um.getUniversity().getNameKh());
            entry.put("locationCity",   um.getUniversity().getLocationCity());
            entry.put("logoUrl",        um.getUniversity().getLogoUrl());
            entry.put("websiteUrl",     um.getUniversity().getWebsiteUrl());
            entry.put("type",           um.getUniversity().getType());
            entry.put("tuitionFeeUsd",  um.getTuitionFeeUsd());
            entry.put("durationYears",  um.getDurationYears());
            return entry;
        }).toList();

        return ResponseEntity.ok(Map.of(
                "selectedMajor", buildMajorMap(profile.getSelectedMajor()),
                "universities",  response
        ));
    }

    private Map<String, Object> buildMajorMap(Major m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("majorId",       m.getId());
        map.put("code",          m.getCode());
        map.put("nameEn",        m.getNameEn());
        map.put("nameKh",        m.getNameKh());
        map.put("descriptionEn", m.getDescriptionEn());
        map.put("descriptionKh", m.getDescriptionKh());
        return map;
    }
}
