package com.sakollife.controller;

import com.sakollife.entity.*;
import com.sakollife.entity.enums.UniversityType;
import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
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
    private final SavedMajorRepository savedMajorRepository;
    private final GuestMajorSelectionRepository guestMajorSelectionRepository;

    //  GET /api/v1/selected-major

    /**
     * Returns the currently selected major.
     * Works for both guests (X-Guest-Session-Id header) and registered users (JWT).
     *
     * Response when selected:   { "selected": true,  "major": { ... } }
     * Response when not set:    { "selected": false, "major": null }
     */
    @GetMapping
    public ResponseEntity<?> getSelectedMajor(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        // Registered user path
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            Profile profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Profile not found"));

            if (profile.getSelectedMajor() == null) {
                return ResponseEntity.ok(Map.of("selected", false, "major", Collections.emptyMap()));
            }
            return ResponseEntity.ok(Map.of(
                    "selected", true,
                    "major", buildMajorMap(profile.getSelectedMajor())
            ));
        }

        // Guest path
        if (guestSessionId != null) {
            return guestMajorSelectionRepository.findByGuestSessionId(guestSessionId)
                    .map(s -> ResponseEntity.ok((Object) Map.of(
                            "selected", true,
                            "major", buildMajorMap(s.getMajor())
                    )))
                    .orElse(ResponseEntity.ok(Map.of("selected", false, "major", Collections.emptyMap())));
        }

        return ResponseEntity.badRequest()
                .body(Map.of("error", "Provide either a valid JWT or X-Guest-Session-Id header"));
    }

    // PUT /api/v1/selected-major

    /**
     * Select (or change) a major. This is the SINGLE endpoint for all user types.
     *
     * Body: { "majorId": "uuid" }
     *
     * For registered users:
     *   - Sets Profile.selectedMajor  ← THIS is what drives the University tab
     *   - Also upserts a SavedMajor row for history
     *
     * For guests:
     *   - Replaces any prior guest_major_selections row for this session
     *   - Returns the same university list so the UI can open the University tab
     *
     * Response 200:
     * {
     *   "selected": true,
     *   "major": { majorId, code, nameEn, nameKh, descriptionEn, descriptionKh,
     *              careerCategory, jobOutlook },
     *   "universities": [ { universityId, nameEn, nameKh, type, locationCity,
     *                        logoUrl, bannerUrl, websiteUrl,
     *                        tuitionFeeUsd, durationYears } ]
     * }
     */
    @PutMapping
    @Transactional
    public ResponseEntity<?> selectMajor(
            @RequestBody Map<String, String> body,
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        UUID majorId = UUID.fromString(body.get("majorId"));
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NoSuchElementException("Major not found: " + majorId));

        // Registered user
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            Profile profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Profile not found"));

            // 1. Update Profile.selectedMajor, this is what GET /api/v1/profile returns
            profile.setSelectedMajor(major);
            profileRepository.save(profile);

            // 2. Upsert SavedMajor for history (keeps a record even if they change later)
            if (!savedMajorRepository.existsByUserIdAndMajorId(userId, majorId)) {
                savedMajorRepository.save(
                        SavedMajor.builder().user(profile).major(major).build()
                );
            }
        }
        // Guest
        else if (guestSessionId != null) {
            // One selection per guest session,replace any existing
            guestMajorSelectionRepository.deleteByGuestSessionId(guestSessionId);
            guestMajorSelectionRepository.save(
                    GuestMajorSelection.builder()
                            .guestSessionId(guestSessionId)
                            .major(major)
                            .build()
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either a valid JWT or X-Guest-Session-Id header"));
        }

        // Return major + university list (same for both paths)
        List<Map<String, Object>> universities = buildUniversityList(majorId, null, null, null, null);

        return ResponseEntity.ok(Map.of(
                "selected",    true,
                "major",       buildMajorMap(major),
                "universities", universities
        ));
    }

    // DELETE /api/v1/selected-major

    /**
     * Deselects the current major.
     * For registered users: clears Profile.selectedMajor.
     * For guests: removes the guest_major_selections row.
     *
     * Response 200: { "selected": false, "message": "Major deselected" }
     */
    @DeleteMapping
    @Transactional
    public ResponseEntity<?> deselectMajor(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            Profile profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Profile not found"));
            profile.setSelectedMajor(null);
            profileRepository.save(profile);

        } else if (guestSessionId != null) {
            guestMajorSelectionRepository.deleteByGuestSessionId(guestSessionId);

        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either a valid JWT or X-Guest-Session-Id header"));
        }

        return ResponseEntity.ok(Map.of("selected", false, "message", "Major deselected"));
    }

    // GET /api/v1/selected-major/universities

    /**
     * Returns universities for the currently selected major.
     * Supports filter query params: type, city, maxFee, durationYears.
     *
     * Works for both registered users and guests.
     * Returns 409 if no major is selected.
     *
     * Response 200:
     * {
     *   "selectedMajor": { ... },
     *   "universities": [ ... ]
     * }
     */
    @GetMapping("/universities")
    public ResponseEntity<?> getUniversitiesForSelectedMajor(
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId,
            @RequestParam(required = false) UniversityType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) Integer durationYears) {

        Major selectedMajor = null;

        // Registered user path
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            Profile profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Profile not found"));
            selectedMajor = profile.getSelectedMajor();
        }
        // Guest path
        else if (guestSessionId != null) {
            selectedMajor = guestMajorSelectionRepository
                    .findByGuestSessionId(guestSessionId)
                    .map(GuestMajorSelection::getMajor)
                    .orElse(null);
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Provide either a valid JWT or X-Guest-Session-Id header"));
        }

        if (selectedMajor == null) {
            return ResponseEntity.status(409).body(Map.of(
                    "error",   "No major selected",
                    "message", "Select a major first before viewing universities"
            ));
        }

        List<Map<String, Object>> universities =
                buildUniversityList(selectedMajor.getId(), type, city, maxFee, durationYears);

        return ResponseEntity.ok(Map.of(
                "selectedMajor", buildMajorMap(selectedMajor),
                "universities",  universities
        ));
    }

    private Map<String, Object> buildMajorMap(Major m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("majorId",        m.getId());
        map.put("code",           m.getCode());
        map.put("nameEn",         m.getNameEn());
        map.put("nameKh",         m.getNameKh());
        map.put("descriptionEn",  m.getDescriptionEn());
        map.put("descriptionKh",  m.getDescriptionKh());
        map.put("careerCategory", m.getCareerCategory());   // NEW — filter chip
        map.put("jobOutlook",     m.getJobOutlook());       // NEW — badge
        return map;
    }

    private List<Map<String, Object>> buildUniversityList(
            UUID majorId,
            UniversityType type,
            String city,
            BigDecimal maxFee,
            Integer durationYears) {

        return universityMajorRepository
                .findByMajorWithFilters(majorId, type, city, maxFee, durationYears)
                .stream()
                .map(um -> {
                    var u = um.getUniversity();
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("universityId",    u.getId());
                    entry.put("nameEn",          u.getNameEn());
                    entry.put("nameKh",          u.getNameKh());
                    entry.put("locationCity",    u.getLocationCity());
                    entry.put("type",            u.getType());
                    entry.put("logoUrl",         u.getLogoUrl());
                    entry.put("bannerUrl",       u.getBannerUrl());   // NEW — for card image
                    entry.put("websiteUrl",      u.getWebsiteUrl());
                    entry.put("tuitionFeeUsd",   um.getTuitionFeeUsd());
                    entry.put("durationYears",   um.getDurationYears());
                    return entry;
                })
                .toList();
    }
}