package com.sakollife.controller;

import com.sakollife.entity.enums.CareerCategory;
import com.sakollife.entity.enums.JobOutlook;
import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
public class MajorController {

    private final MajorRepository majorRepository;
    private final QuizResultRepository quizResultRepository;

    /**
     * GET /api/v1/majors
     *
     * Returns all 9 majors. Supports optional filter params:
     *   ?careerCategory=SOFTWARE_ENGINEERING
     *   ?jobOutlook=HIGH
     *
     * Used to in the majors browse page (not the quiz results page).
     */
    @GetMapping
    public ResponseEntity<?> getAllMajors(
            @RequestParam(required = false) CareerCategory careerCategory,
            @RequestParam(required = false) JobOutlook jobOutlook) {

        var majors = majorRepository.findAll().stream()
                .filter(m -> careerCategory == null || careerCategory.equals(m.getCareerCategory()))
                .filter(m -> jobOutlook == null || jobOutlook.equals(m.getJobOutlook()))
                .map(m -> {
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("majorId",        m.getId());
                    map.put("code",           m.getCode());
                    map.put("nameEn",         m.getNameEn());
                    map.put("nameKh",         m.getNameKh());
                    map.put("descriptionEn",  m.getDescriptionEn());
                    map.put("descriptionKh",  m.getDescriptionKh());
                    map.put("careerCategory", m.getCareerCategory());
                    map.put("jobOutlook",     m.getJobOutlook());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(majors);
    }

    /**
     * GET /api/v1/majors/{id}
     * Returns a single major by UUID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMajor(@PathVariable UUID id) {
        return majorRepository.findById(id)
                .map(m -> {
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("majorId",        m.getId());
                    map.put("code",           m.getCode());
                    map.put("nameEn",         m.getNameEn());
                    map.put("nameKh",         m.getNameKh());
                    map.put("descriptionEn",  m.getDescriptionEn());
                    map.put("descriptionKh",  m.getDescriptionKh());
                    map.put("careerCategory", m.getCareerCategory());
                    map.put("jobOutlook",     m.getJobOutlook());
                    return ResponseEntity.ok((Object) map);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/majors/results/{attemptId}
     */
    @GetMapping("/results/{attemptId}")
    public ResponseEntity<?> getResults(
            @PathVariable UUID attemptId,
            @RequestParam(required = false) CareerCategory careerCategory,
            @RequestParam(required = false) JobOutlook jobOutlook,
            @RequestParam(required = false, defaultValue = "0.5") double minScore) {

        var results = quizResultRepository.findQualifyingResultsByAttemptId(attemptId);
        if (results.isEmpty()) return ResponseEntity.notFound().build();

        var filtered = results.stream()
                .filter(r -> r.getSimilarityScore().doubleValue() >= minScore)
                .filter(r -> careerCategory == null
                        || careerCategory.equals(r.getMajor().getCareerCategory()))
                .filter(r -> jobOutlook == null
                        || jobOutlook.equals(r.getMajor().getJobOutlook()))
                .map(r -> {
                    var m = r.getMajor();
                    var map = new java.util.LinkedHashMap<String, Object>();
                    map.put("majorId",              m.getId());
                    map.put("code",                 m.getCode());
                    map.put("nameEn",               m.getNameEn());
                    map.put("nameKh",               m.getNameKh());
                    map.put("descriptionEn",         m.getDescriptionEn());
                    map.put("descriptionKh",         m.getDescriptionKh());
                    map.put("careerCategory",        m.getCareerCategory());   // NEW
                    map.put("jobOutlook",            m.getJobOutlook());       // NEW
                    map.put("rank",                  r.getRank());
                    map.put("similarityScore",       r.getSimilarityScore());
                    map.put("similarityPercentage",
                            (int) Math.round(r.getSimilarityScore().doubleValue() * 100));
                    return map;
                })
                .toList();

        return ResponseEntity.ok(filtered);
    }
}