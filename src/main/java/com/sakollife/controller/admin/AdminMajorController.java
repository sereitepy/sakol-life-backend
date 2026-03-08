package com.sakollife.controller.admin;

import com.sakollife.entity.Major;
import com.sakollife.repository.MajorRepository;
import com.sakollife.repository.QuizResultRepository;
import com.sakollife.repository.UniversityMajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/majors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMajorController {

    private final MajorRepository majorRepository;
    private final QuizResultRepository quizResultRepository;
    private final UniversityMajorRepository universityMajorRepository;

    /**
     * GET /api/v1/admin/majors
     * List all 9 majors with full details including RIASEC vectors.
     */
    @GetMapping
    public ResponseEntity<?> getAllMajors() {
        return ResponseEntity.ok(majorRepository.findAll());
    }

    /**
     * GET /api/v1/admin/majors/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMajor(@PathVariable UUID id) {
        return majorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/v1/admin/majors/{id}
     * Update major descriptions (EN/KH) and/or RIASEC vector values.
     *
     * Body example (all fields optional — only send what you want to change):
     * {
     *   "nameEn": "Computer Science",
     *   "nameKh": "វិទ្យាសាស្ត្រកុំព្យូទ័រ",
     *   "descriptionEn": "Updated description...",
     *   "descriptionKh": "...",
     *   "riasecR": 1.0,
     *   "riasecI": 1.0,
     *   "riasecA": 0.1,
     *   "riasecS": 0.1,
     *   "riasecE": 0.5,
     *   "riasecC": 1.0
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMajor(@PathVariable UUID id,
                                          @RequestBody Map<String, Object> body) {
        Major major = majorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Major not found"));

        if (body.containsKey("nameEn"))        major.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))        major.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("descriptionEn")) major.setDescriptionEn((String) body.get("descriptionEn"));
        if (body.containsKey("descriptionKh")) major.setDescriptionKh((String) body.get("descriptionKh"));
        if (body.containsKey("riasecR"))       major.setRiasecR(bd(body, "riasecR"));
        if (body.containsKey("riasecI"))       major.setRiasecI(bd(body, "riasecI"));
        if (body.containsKey("riasecA"))       major.setRiasecA(bd(body, "riasecA"));
        if (body.containsKey("riasecS"))       major.setRiasecS(bd(body, "riasecS"));
        if (body.containsKey("riasecE"))       major.setRiasecE(bd(body, "riasecE"));
        if (body.containsKey("riasecC"))       major.setRiasecC(bd(body, "riasecC"));

        return ResponseEntity.ok(majorRepository.save(major));
    }

    /**
     * POST /api/v1/admin/majors
     * Create a new major. Useful when the platform expands beyond 9 majors.
     * Body example:
     * {
     *   "code": "CE",
     *   "nameEn": "Computer Engineering",
     *   "nameKh": "វិស្វកម្មកុំព្យូទ័រ",
     *   "descriptionEn": "Focuses on hardware and software integration...",
     *   "descriptionKh": "...",
     *   "riasecR": 1.0,
     *   "riasecI": 1.0,
     *   "riasecA": 0.1,
     *   "riasecS": 0.1,
     *   "riasecE": 0.5,
     *   "riasecC": 1.0
     * }
     */
    @PostMapping
    public ResponseEntity<?> createMajor(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("code");
        if (majorRepository.findByCode(code).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Major with code '" + code + "' already exists"));
        }

        Major major = Major.builder()
                .code(code)
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .descriptionEn((String) body.getOrDefault("descriptionEn", ""))
                .descriptionKh((String) body.getOrDefault("descriptionKh", ""))
                .riasecR(bd(body, "riasecR"))
                .riasecI(bd(body, "riasecI"))
                .riasecA(bd(body, "riasecA"))
                .riasecS(bd(body, "riasecS"))
                .riasecE(bd(body, "riasecE"))
                .riasecC(bd(body, "riasecC"))
                .build();

        return ResponseEntity.ok(majorRepository.save(major));
    }

    /**
     * DELETE /api/v1/admin/majors/{id}
     * Delete a major. Use with caution — this will cascade-delete any quiz_results
     * referencing this major. Safe to use freely during development.
     * In production, consider soft-delete by adding an 'active' flag to Major.
     */
    @DeleteMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteMajor(@PathVariable UUID id) {
        if (!majorRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Must delete in this exact order due to FK constraints
        universityMajorRepository.deleteByMajorId(id);
        quizResultRepository.deleteByMajorId(id);
        majorRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Major deleted", "id", id));
    }

    private BigDecimal bd(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return BigDecimal.ZERO;
        return new BigDecimal(val.toString());
    }
}
