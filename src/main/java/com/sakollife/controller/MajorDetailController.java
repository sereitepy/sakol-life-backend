package com.sakollife.controller;

import com.sakollife.entity.*;
import com.sakollife.entity.enums.CareerCategory;
import com.sakollife.entity.enums.JobDemandLevel;
import com.sakollife.entity.enums.SkillType;
import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * GET /api/v1/majors/{id}/detail
 * Powers the individual Major detail page.
 */
@RestController
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
public class MajorDetailController {

    private final MajorRepository majorRepository;
    private final MajorSubjectRepository majorSubjectRepository;
    private final MajorSkillRepository majorSkillRepository;
    private final MajorCareerOpportunityRepository majorCareerOpportunityRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getMajorDetail(
            @PathVariable UUID id,
            Authentication authentication) {

        Major major = majorRepository.findById(id).orElse(null);
        if (major == null) return ResponseEntity.notFound().build();

        Integer similarityPercentage = null;
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            var latestAttempt = quizAttemptRepository.findLatestByUserId(userId);
            if (latestAttempt.isPresent()) {
                var results = quizResultRepository.findQualifyingResultsByAttemptId(latestAttempt.get().getId());
                results.stream()
                        .filter(r -> r.getMajor().getId().equals(id))
                        .findFirst()
                        .ifPresent(r -> {
                        });
                similarityPercentage = results.stream()
                        .filter(r -> r.getMajor().getId().equals(id))
                        .findFirst()
                        .map(r -> (int) Math.round(r.getSimilarityScore().doubleValue() * 100))
                        .orElse(null);
            }
        }

        List<Map<String, Object>> subjects = majorSubjectRepository
                .findByMajorIdOrderByDisplayOrderAsc(id).stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId()); m.put("nameEn", s.getNameEn()); m.put("nameKh", s.getNameKh());
                    m.put("descriptionEn", s.getDescriptionEn()); m.put("descriptionKh", s.getDescriptionKh());
                    m.put("iconKey", s.getIconKey()); m.put("displayOrder", s.getDisplayOrder());
                    return m;
                }).toList();

        List<Map<String, Object>> technicalSkills = majorSkillRepository
                .findByMajorIdAndSkillTypeOrderByDisplayOrderAsc(id, SkillType.TECHNICAL)
                .stream().map(this::buildSkillMap).toList();

        List<Map<String, Object>> softSkills = majorSkillRepository
                .findByMajorIdAndSkillTypeOrderByDisplayOrderAsc(id, SkillType.SOFT)
                .stream().map(this::buildSkillMap).toList();

        List<Map<String, Object>> careerOpportunities = majorCareerOpportunityRepository
                .findByMajorIdOrderByDisplayOrderAsc(id).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId()); m.put("titleEn", c.getTitleEn());
                    m.put("titleKh", c.getTitleKh()); m.put("iconKey", c.getIconKey());
                    return m;
                }).toList();

        List<Map<String, Object>> relatedMajors = majorRepository.findAll().stream()
                .filter(m -> !m.getId().equals(id))
                .limit(3)
                .map(m -> {
                    Map<String, Object> rm = new LinkedHashMap<>();
                    rm.put("majorId", m.getId()); rm.put("code", m.getCode());
                    rm.put("nameEn", m.getNameEn()); rm.put("nameKh", m.getNameKh());
                    rm.put("careerCategory", m.getCareerCategory()); rm.put("iconUrl", m.getIconUrl());
                    return rm;
                }).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("majorId", major.getId());
        response.put("code", major.getCode());
        response.put("nameEn", major.getNameEn());
        response.put("nameKh", major.getNameKh());
        response.put("faculty", major.getFaculty());
        response.put("degreeType", major.getDegreeType());
        response.put("language", major.getLanguage());
        response.put("iconUrl", major.getIconUrl());
        response.put("descriptionEn", major.getDescriptionEn());
        response.put("descriptionKh", major.getDescriptionKh());
        response.put("careerCategory", major.getCareerCategory());
        response.put("jobOutlook", major.getJobOutlook());
        response.put("updatedAt", major.getUpdatedAt());
        response.put("similarityPercentage", similarityPercentage);
        response.put("subjects", subjects);
        response.put("technicalSkills", technicalSkills);
        response.put("softSkills", softSkills);
        response.put("careerOpportunities", careerOpportunities);
        response.put("jobMarket", Map.of(
                "demandLevel", major.getJobDemandLevel() != null ? major.getJobDemandLevel() : JobDemandLevel.MEDIUM,
                "salaryMin", major.getSalaryMin() != null ? major.getSalaryMin() : 0,
                "salaryMax", major.getSalaryMax() != null ? major.getSalaryMax() : 0,
                "currency", "USD", "period", "month"
        ));
        response.put("relatedMajors", relatedMajors);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> buildSkillMap(MajorSkill s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId()); m.put("nameEn", s.getNameEn());
        m.put("nameKh", s.getNameKh()); m.put("displayOrder", s.getDisplayOrder());
        return m;
    }
}