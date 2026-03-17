package com.sakollife.controller;

import com.sakollife.entity.*;
import com.sakollife.entity.enums.SkillType;
import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin CRUD for Major detail page content.
 * All endpoints require ROLE_ADMIN.
 *
 * Base: /api/v1/admin/majors
 *
 * Subjects:
 *   POST   /{majorId}/subjects
 *   PUT    /{majorId}/subjects/{subjectId}
 *   DELETE /{majorId}/subjects/{subjectId}
 *
 * Skills:
 *   POST   /{majorId}/skills
 *   PUT    /{majorId}/skills/{skillId}
 *   DELETE /{majorId}/skills/{skillId}
 *
 * Career Opportunities:
 *   POST   /{majorId}/career-opportunities
 *   PUT    /{majorId}/career-opportunities/{oppId}
 *   DELETE /{majorId}/career-opportunities/{oppId}
 *
 * Major fields (faculty, degreeType, language, jobDemandLevel, salary, iconUrl):
 *   PATCH  /{majorId}
 */
@RestController
@RequestMapping("/api/v1/admin/majors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMajorController {

    private final MajorRepository majorRepository;
    private final MajorSubjectRepository majorSubjectRepository;
    private final MajorSkillRepository majorSkillRepository;
    private final MajorCareerOpportunityRepository majorCareerOpportunityRepository;

    // ── PATCH /{majorId} — update major top-level fields ─────────────────────

    /**
     * Update top-level major fields.
     * Body (all optional):
     * {
     *   "faculty": "Faculty of Engineering & Technology",
     *   "degreeType": "Bachelor of Science",
     *   "language": "English / Khmer",
     *   "iconUrl": "https://...",
     *   "jobDemandLevel": "VERY_HIGH",
     *   "salaryMin": 500,
     *   "salaryMax": 900
     * }
     */
    @PatchMapping("/{majorId}")
    public ResponseEntity<?> updateMajor(
            @PathVariable UUID majorId,
            @RequestBody Map<String, Object> body) {

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new NoSuchElementException("Major not found: " + majorId));

        if (body.containsKey("faculty"))       major.setFaculty((String) body.get("faculty"));
        if (body.containsKey("degreeType"))    major.setDegreeType((String) body.get("degreeType"));
        if (body.containsKey("language"))      major.setLanguage((String) body.get("language"));
        if (body.containsKey("iconUrl"))       major.setIconUrl((String) body.get("iconUrl"));
        if (body.containsKey("jobDemandLevel"))
            major.setJobDemandLevel(
                    com.sakollife.entity.enums.JobDemandLevel.valueOf((String) body.get("jobDemandLevel")));
        if (body.containsKey("salaryMin"))     major.setSalaryMin((Integer) body.get("salaryMin"));
        if (body.containsKey("salaryMax"))     major.setSalaryMax((Integer) body.get("salaryMax"));

        majorRepository.save(major);
        return ResponseEntity.ok(Map.of("message", "Major updated", "majorId", majorId));
    }

    // ── SUBJECTS ──────────────────────────────────────────────────────────────

    /**
     * POST /{majorId}/subjects
     * Body: { "nameEn": "...", "nameKh": "...", "descriptionEn": "...",
     *         "descriptionKh": "...", "iconKey": "code", "displayOrder": 0 }
     */
    @PostMapping("/{majorId}/subjects")
    public ResponseEntity<?> addSubject(
            @PathVariable UUID majorId,
            @RequestBody Map<String, Object> body) {

        Major major = majorRepository.getReferenceById(majorId);
        MajorSubject subject = MajorSubject.builder()
                .major(major)
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .descriptionEn((String) body.get("descriptionEn"))
                .descriptionKh((String) body.get("descriptionKh"))
                .iconKey((String) body.get("iconKey"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();

        majorSubjectRepository.save(subject);
        return ResponseEntity.status(201).body(Map.of("id", subject.getId(), "message", "Subject added"));
    }

    @PutMapping("/{majorId}/subjects/{subjectId}")
    public ResponseEntity<?> updateSubject(
            @PathVariable UUID majorId,
            @PathVariable UUID subjectId,
            @RequestBody Map<String, Object> body) {

        MajorSubject subject = majorSubjectRepository.findById(subjectId)
                .orElseThrow(() -> new NoSuchElementException("Subject not found"));

        if (body.containsKey("nameEn"))        subject.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))        subject.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("descriptionEn")) subject.setDescriptionEn((String) body.get("descriptionEn"));
        if (body.containsKey("descriptionKh")) subject.setDescriptionKh((String) body.get("descriptionKh"));
        if (body.containsKey("iconKey"))       subject.setIconKey((String) body.get("iconKey"));
        if (body.containsKey("displayOrder"))  subject.setDisplayOrder((Integer) body.get("displayOrder"));

        majorSubjectRepository.save(subject);
        return ResponseEntity.ok(Map.of("message", "Subject updated"));
    }

    @DeleteMapping("/{majorId}/subjects/{subjectId}")
    public ResponseEntity<?> deleteSubject(@PathVariable UUID majorId, @PathVariable UUID subjectId) {
        majorSubjectRepository.deleteById(subjectId);
        return ResponseEntity.ok(Map.of("message", "Subject deleted"));
    }

    // ── SKILLS ────────────────────────────────────────────────────────────────

    /**
     * POST /{majorId}/skills
     * Body: { "skillType": "TECHNICAL", "nameEn": "Java / Python / C++",
     *         "nameKh": "...", "displayOrder": 0 }
     */
    @PostMapping("/{majorId}/skills")
    public ResponseEntity<?> addSkill(
            @PathVariable UUID majorId,
            @RequestBody Map<String, Object> body) {

        Major major = majorRepository.getReferenceById(majorId);
        MajorSkill skill = MajorSkill.builder()
                .major(major)
                .skillType(SkillType.valueOf((String) body.get("skillType")))
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();

        majorSkillRepository.save(skill);
        return ResponseEntity.status(201).body(Map.of("id", skill.getId(), "message", "Skill added"));
    }

    @PutMapping("/{majorId}/skills/{skillId}")
    public ResponseEntity<?> updateSkill(
            @PathVariable UUID majorId,
            @PathVariable UUID skillId,
            @RequestBody Map<String, Object> body) {

        MajorSkill skill = majorSkillRepository.findById(skillId)
                .orElseThrow(() -> new NoSuchElementException("Skill not found"));

        if (body.containsKey("skillType"))    skill.setSkillType(SkillType.valueOf((String) body.get("skillType")));
        if (body.containsKey("nameEn"))       skill.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))       skill.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("displayOrder")) skill.setDisplayOrder((Integer) body.get("displayOrder"));

        majorSkillRepository.save(skill);
        return ResponseEntity.ok(Map.of("message", "Skill updated"));
    }

    @DeleteMapping("/{majorId}/skills/{skillId}")
    public ResponseEntity<?> deleteSkill(@PathVariable UUID majorId, @PathVariable UUID skillId) {
        majorSkillRepository.deleteById(skillId);
        return ResponseEntity.ok(Map.of("message", "Skill deleted"));
    }

    // ── CAREER OPPORTUNITIES ──────────────────────────────────────────────────

    /**
     * POST /{majorId}/career-opportunities
     * Body: { "titleEn": "Software Developer", "titleKh": "...",
     *         "iconKey": "monitor", "displayOrder": 0 }
     */
    @PostMapping("/{majorId}/career-opportunities")
    public ResponseEntity<?> addCareerOpportunity(
            @PathVariable UUID majorId,
            @RequestBody Map<String, Object> body) {

        Major major = majorRepository.getReferenceById(majorId);
        MajorCareerOpportunity opp = MajorCareerOpportunity.builder()
                .major(major)
                .titleEn((String) body.get("titleEn"))
                .titleKh((String) body.get("titleKh"))
                .iconKey((String) body.get("iconKey"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();

        majorCareerOpportunityRepository.save(opp);
        return ResponseEntity.status(201).body(Map.of("id", opp.getId(), "message", "Career opportunity added"));
    }

    @PutMapping("/{majorId}/career-opportunities/{oppId}")
    public ResponseEntity<?> updateCareerOpportunity(
            @PathVariable UUID majorId,
            @PathVariable UUID oppId,
            @RequestBody Map<String, Object> body) {

        MajorCareerOpportunity opp = majorCareerOpportunityRepository.findById(oppId)
                .orElseThrow(() -> new NoSuchElementException("Career opportunity not found"));

        if (body.containsKey("titleEn"))      opp.setTitleEn((String) body.get("titleEn"));
        if (body.containsKey("titleKh"))      opp.setTitleKh((String) body.get("titleKh"));
        if (body.containsKey("iconKey"))      opp.setIconKey((String) body.get("iconKey"));
        if (body.containsKey("displayOrder")) opp.setDisplayOrder((Integer) body.get("displayOrder"));

        majorCareerOpportunityRepository.save(opp);
        return ResponseEntity.ok(Map.of("message", "Career opportunity updated"));
    }

    @DeleteMapping("/{majorId}/career-opportunities/{oppId}")
    public ResponseEntity<?> deleteCareerOpportunity(@PathVariable UUID majorId, @PathVariable UUID oppId) {
        majorCareerOpportunityRepository.deleteById(oppId);
        return ResponseEntity.ok(Map.of("message", "Career opportunity deleted"));
    }
}