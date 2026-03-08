package com.sakollife.controller.admin;

import com.sakollife.entity.Major;
import com.sakollife.entity.University;
import com.sakollife.entity.UniversityMajor;
import com.sakollife.entity.enums.UniversityType;
import com.sakollife.repository.MajorRepository;
import com.sakollife.repository.UniversityMajorRepository;
import com.sakollife.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/universities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUniversityController {

    private final UniversityRepository universityRepository;
    private final UniversityMajorRepository universityMajorRepository;
    private final MajorRepository majorRepository;

    /**
     * GET /api/v1/admin/universities
     */
    @GetMapping
    public ResponseEntity<?> getAllUniversities() {
        return ResponseEntity.ok(universityRepository.findAll());
    }

    /**
     * GET /api/v1/admin/universities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUniversity(@PathVariable UUID id) {
        return universityRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/admin/universities
     * Create a new university.
     * Body:
     * {
     *   "nameEn": "Royal University of Phnom Penh",
     *   "nameKh": "សកលវិទ្យាល័យភូមិន្ទភ្នំពេញ",
     *   "locationCity": "Phnom Penh",
     *   "logoUrl": "/images/universities/rupp.png",
     *   "websiteUrl": "https://rupp.edu.kh",
     *   "type": "PUBLIC"
     * }
     */
    @PostMapping
    public ResponseEntity<?> createUniversity(@RequestBody Map<String, Object> body) {
        University university = University.builder()
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .locationCity((String) body.get("locationCity"))
                .logoUrl((String) body.getOrDefault("logoUrl", ""))
                .websiteUrl((String) body.getOrDefault("websiteUrl", ""))
                .type(UniversityType.valueOf((String) body.get("type")))
                .build();

        return ResponseEntity.ok(universityRepository.save(university));
    }

    /**
     * PUT /api/v1/admin/universities/{id}
     * Update university details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUniversity(@PathVariable UUID id,
                                               @RequestBody Map<String, Object> body) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found"));

        if (body.containsKey("nameEn"))       university.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))       university.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("locationCity")) university.setLocationCity((String) body.get("locationCity"));
        if (body.containsKey("logoUrl"))      university.setLogoUrl((String) body.get("logoUrl"));
        if (body.containsKey("websiteUrl"))   university.setWebsiteUrl((String) body.get("websiteUrl"));
        if (body.containsKey("type"))         university.setType(UniversityType.valueOf((String) body.get("type")));

        return ResponseEntity.ok(universityRepository.save(university));
    }

    /**
     * DELETE /api/v1/admin/universities/{id}
     * Delete this specific major, including all the major lists it has
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUniversity(@PathVariable UUID id) {
        if (!universityRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Delete university_majors rows first to avoid FK violation
        universityMajorRepository.deleteByUniversityId(id);
        universityRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "University deleted", "id", id));
    }

    /**
     * GET /api/v1/admin/universities/{id}/majors
     * List all majors offered by a specific university.
     */
    @GetMapping("/{id}/majors")
    public ResponseEntity<?> getUniversityMajors(@PathVariable UUID id) {
        return ResponseEntity.ok(
                universityMajorRepository.findByMajorWithFilters(id, null, null, null, null)
        );
    }

    /**
     * POST /api/v1/admin/universities/{universityId}/majors
     * Link a major to a university with tuition and duration.
     * Body:
     * {
     *   "majorId": "uuid-of-major",
     *   "tuitionFeeUsd": 1500.00,
     *   "durationYears": 4
     * }
     */
    @PostMapping("/{universityId}/majors")
    public ResponseEntity<?> addMajorToUniversity(@PathVariable UUID universityId,
                                                   @RequestBody Map<String, Object> body) {
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new NoSuchElementException("University not found"));

        UUID majorId = UUID.fromString((String) body.get("majorId"));
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NoSuchElementException("Major not found"));

        UniversityMajor um = UniversityMajor.builder()
                .university(university)
                .major(major)
                .tuitionFeeUsd(body.containsKey("tuitionFeeUsd")
                        ? new BigDecimal(body.get("tuitionFeeUsd").toString()) : null)
                .durationYears(body.containsKey("durationYears")
                        ? (Integer) body.get("durationYears") : null)
                .build();

        return ResponseEntity.ok(universityMajorRepository.save(um));
    }

    /**
     * PUT /api/v1/admin/universities/{universityId}/majors/{universityMajorId}
     * Update tuition fee or duration for a university-major offering.
     */
    @PutMapping("/{universityId}/majors/{universityMajorId}")
    public ResponseEntity<?> updateUniversityMajor(@PathVariable UUID universityId,
                                                    @PathVariable UUID universityMajorId,
                                                    @RequestBody Map<String, Object> body) {
        UniversityMajor um = universityMajorRepository.findById(universityMajorId)
                .orElseThrow(() -> new NoSuchElementException("University-major offering not found"));

        if (body.containsKey("tuitionFeeUsd"))
            um.setTuitionFeeUsd(new BigDecimal(body.get("tuitionFeeUsd").toString()));
        if (body.containsKey("durationYears"))
            um.setDurationYears((Integer) body.get("durationYears"));

        return ResponseEntity.ok(universityMajorRepository.save(um));
    }

    /**
     * DELETE /api/v1/admin/universities/{universityId}/majors/{universityMajorId}
     * Remove a major offering from a university.
     */
    @DeleteMapping("/{universityId}/majors/{universityMajorId}")
    public ResponseEntity<?> removeUniversityMajor(@PathVariable UUID universityId,
                                                    @PathVariable UUID universityMajorId) {
        universityMajorRepository.deleteById(universityMajorId);
        return ResponseEntity.ok(Map.of("message", "Major offering removed", "id", universityMajorId));
    }
}
