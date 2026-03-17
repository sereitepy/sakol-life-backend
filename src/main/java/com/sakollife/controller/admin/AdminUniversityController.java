package com.sakollife.controller;

import com.sakollife.entity.*;
import com.sakollife.repository.*;
import com.sakollife.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Admin CRUD for University detail page content.
 * All endpoints require ROLE_ADMIN.
 *
 * Base: /api/v1/admin/universities
 *
 * University fields:   PATCH /{id}
 * Banner upload:       POST  /{id}/upload-banner  (multipart)
 * Banner delete:       DELETE /{id}/banner
 *
 * Admission:           POST/PUT/DELETE /{id}/admissions/{reqId}
 * Tuition:             POST/PUT/DELETE /{id}/tuition/{feeId}
 * Scholarships:        POST/PUT/DELETE /{id}/scholarships/{scholarshipId}
 * Facilities:          POST/PUT/DELETE /{id}/facilities/{facilityId}
 * Facility photos:     POST /{id}/facility-photos/upload  (multipart)
 *                      DELETE /{id}/facility-photos/{photoId}
 *
 * UniversityMajor:     PATCH /{id}/university-majors/{umId}
 * UM Career Prospects: POST/DELETE /{id}/university-majors/{umId}/prospects
 */
@RestController
@RequestMapping("/api/v1/admin/universities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUniversityController {

    private final UniversityRepository universityRepository;
    private final UniversityMajorRepository universityMajorRepository;
    private final UniversityMajorCareerProspectRepository careerProspectRepository;
    private final AdmissionRequirementRepository admissionRequirementRepository;
    private final TuitionFeeStructureRepository tuitionFeeStructureRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityPhotoRepository facilityPhotoRepository;
    private final StorageService storageService;

    // ── PATCH /{id} — update university overview fields ───────────────────────

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateUniversity(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        University u = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found"));

        if (body.containsKey("nameEn"))        u.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))        u.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("locationCity"))  u.setLocationCity((String) body.get("locationCity"));
        if (body.containsKey("logoUrl"))       u.setLogoUrl((String) body.get("logoUrl"));
        if (body.containsKey("websiteUrl"))    u.setWebsiteUrl((String) body.get("websiteUrl"));
        if (body.containsKey("accreditation")) u.setAccreditation((String) body.get("accreditation"));
        if (body.containsKey("overviewEn"))    u.setOverviewEn((String) body.get("overviewEn"));
        if (body.containsKey("overviewKh"))    u.setOverviewKh((String) body.get("overviewKh"));
        if (body.containsKey("establishedDate"))
            u.setEstablishedDate(LocalDate.parse((String) body.get("establishedDate")));

        universityRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "University updated"));
    }

    // ── BANNER ────────────────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/upload-banner", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadBanner(@PathVariable UUID id, @RequestParam("file") MultipartFile file) {
        University u = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found"));
        String url = storageService.uploadUniversityBanner(id, file);
        u.setBannerUrl(url);
        universityRepository.save(u);
        return ResponseEntity.ok(Map.of("universityId", id, "bannerUrl", url));
    }

    @DeleteMapping("/{id}/banner")
    public ResponseEntity<?> deleteBanner(@PathVariable UUID id) {
        University u = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found"));
        if (u.getBannerUrl() != null) {
            storageService.deleteByUrl(u.getBannerUrl());
            u.setBannerUrl(null);
            universityRepository.save(u);
        }
        return ResponseEntity.ok(Map.of("message", "Banner removed"));
    }

    // ── ADMISSION REQUIREMENTS ────────────────────────────────────────────────

    @PostMapping("/{id}/admissions")
    public ResponseEntity<?> addAdmission(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        University u = universityRepository.getReferenceById(id);
        AdmissionRequirement req = AdmissionRequirement.builder()
                .university(u)
                .titleEn((String) body.get("titleEn"))
                .titleKh((String) body.get("titleKh"))
                .descriptionEn((String) body.get("descriptionEn"))
                .descriptionKh((String) body.get("descriptionKh"))
                .linkLabelEn((String) body.get("linkLabelEn"))
                .linkUrl((String) body.get("linkUrl"))
                .iconKey((String) body.get("iconKey"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();
        admissionRequirementRepository.save(req);
        return ResponseEntity.status(201).body(Map.of("id", req.getId(), "message", "Admission requirement added"));
    }

    @PutMapping("/{id}/admissions/{reqId}")
    public ResponseEntity<?> updateAdmission(@PathVariable UUID id, @PathVariable UUID reqId,
                                             @RequestBody Map<String, Object> body) {
        AdmissionRequirement req = admissionRequirementRepository.findById(reqId)
                .orElseThrow(() -> new NoSuchElementException("Admission requirement not found"));
        if (body.containsKey("titleEn"))        req.setTitleEn((String) body.get("titleEn"));
        if (body.containsKey("titleKh"))        req.setTitleKh((String) body.get("titleKh"));
        if (body.containsKey("descriptionEn"))  req.setDescriptionEn((String) body.get("descriptionEn"));
        if (body.containsKey("descriptionKh"))  req.setDescriptionKh((String) body.get("descriptionKh"));
        if (body.containsKey("linkLabelEn"))    req.setLinkLabelEn((String) body.get("linkLabelEn"));
        if (body.containsKey("linkUrl"))        req.setLinkUrl((String) body.get("linkUrl"));
        if (body.containsKey("iconKey"))        req.setIconKey((String) body.get("iconKey"));
        if (body.containsKey("displayOrder"))   req.setDisplayOrder((Integer) body.get("displayOrder"));
        admissionRequirementRepository.save(req);
        return ResponseEntity.ok(Map.of("message", "Admission requirement updated"));
    }

    @DeleteMapping("/{id}/admissions/{reqId}")
    public ResponseEntity<?> deleteAdmission(@PathVariable UUID id, @PathVariable UUID reqId) {
        admissionRequirementRepository.deleteById(reqId);
        return ResponseEntity.ok(Map.of("message", "Admission requirement deleted"));
    }

    // ── TUITION FEES ──────────────────────────────────────────────────────────

    @PostMapping("/{id}/tuition")
    public ResponseEntity<?> addTuition(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        University u = universityRepository.getReferenceById(id);
        TuitionFeeStructure fee = TuitionFeeStructure.builder()
                .university(u)
                .programTypeEn((String) body.get("programTypeEn"))
                .programTypeKh((String) body.get("programTypeKh"))
                .feePerSemester(body.containsKey("feePerSemester")
                        ? new BigDecimal(body.get("feePerSemester").toString()) : null)
                .feePerYear(body.containsKey("feePerYear")
                        ? new BigDecimal(body.get("feePerYear").toString()) : null)
                .notesEn((String) body.get("notesEn"))
                .notesKh((String) body.get("notesKh"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();
        tuitionFeeStructureRepository.save(fee);
        return ResponseEntity.status(201).body(Map.of("id", fee.getId(), "message", "Tuition fee added"));
    }

    @PutMapping("/{id}/tuition/{feeId}")
    public ResponseEntity<?> updateTuition(@PathVariable UUID id, @PathVariable UUID feeId,
                                           @RequestBody Map<String, Object> body) {
        TuitionFeeStructure fee = tuitionFeeStructureRepository.findById(feeId)
                .orElseThrow(() -> new NoSuchElementException("Tuition fee not found"));
        if (body.containsKey("programTypeEn"))  fee.setProgramTypeEn((String) body.get("programTypeEn"));
        if (body.containsKey("programTypeKh"))  fee.setProgramTypeKh((String) body.get("programTypeKh"));
        if (body.containsKey("feePerSemester"))
            fee.setFeePerSemester(new BigDecimal(body.get("feePerSemester").toString()));
        if (body.containsKey("feePerYear"))
            fee.setFeePerYear(new BigDecimal(body.get("feePerYear").toString()));
        if (body.containsKey("notesEn"))        fee.setNotesEn((String) body.get("notesEn"));
        if (body.containsKey("notesKh"))        fee.setNotesKh((String) body.get("notesKh"));
        if (body.containsKey("displayOrder"))   fee.setDisplayOrder((Integer) body.get("displayOrder"));
        tuitionFeeStructureRepository.save(fee);
        return ResponseEntity.ok(Map.of("message", "Tuition fee updated"));
    }

    @DeleteMapping("/{id}/tuition/{feeId}")
    public ResponseEntity<?> deleteTuition(@PathVariable UUID id, @PathVariable UUID feeId) {
        tuitionFeeStructureRepository.deleteById(feeId);
        return ResponseEntity.ok(Map.of("message", "Tuition fee deleted"));
    }

    // ── SCHOLARSHIPS ──────────────────────────────────────────────────────────

    @PostMapping("/{id}/scholarships")
    public ResponseEntity<?> addScholarship(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        University u = universityRepository.getReferenceById(id);
        Scholarship s = Scholarship.builder()
                .university(u)
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .coverageLabel((String) body.get("coverageLabel"))
                .descriptionEn((String) body.get("descriptionEn"))
                .descriptionKh((String) body.get("descriptionKh"))
                .deadline(body.containsKey("deadline") ? LocalDate.parse((String) body.get("deadline")) : null)
                .build();
        scholarshipRepository.save(s);
        return ResponseEntity.status(201).body(Map.of("id", s.getId(), "message", "Scholarship added"));
    }

    @PutMapping("/{id}/scholarships/{scholarshipId}")
    public ResponseEntity<?> updateScholarship(@PathVariable UUID id, @PathVariable UUID scholarshipId,
                                               @RequestBody Map<String, Object> body) {
        Scholarship s = scholarshipRepository.findById(scholarshipId)
                .orElseThrow(() -> new NoSuchElementException("Scholarship not found"));
        if (body.containsKey("nameEn"))         s.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))         s.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("coverageLabel"))  s.setCoverageLabel((String) body.get("coverageLabel"));
        if (body.containsKey("descriptionEn"))  s.setDescriptionEn((String) body.get("descriptionEn"));
        if (body.containsKey("descriptionKh"))  s.setDescriptionKh((String) body.get("descriptionKh"));
        if (body.containsKey("deadline"))       s.setDeadline(LocalDate.parse((String) body.get("deadline")));
        scholarshipRepository.save(s);
        return ResponseEntity.ok(Map.of("message", "Scholarship updated"));
    }

    @DeleteMapping("/{id}/scholarships/{scholarshipId}")
    public ResponseEntity<?> deleteScholarship(@PathVariable UUID id, @PathVariable UUID scholarshipId) {
        scholarshipRepository.deleteById(scholarshipId);
        return ResponseEntity.ok(Map.of("message", "Scholarship deleted"));
    }

    // ── FACILITIES ────────────────────────────────────────────────────────────

    @PostMapping("/{id}/facilities")
    public ResponseEntity<?> addFacility(@PathVariable UUID id, @RequestBody Map<String, Object> body) {
        University u = universityRepository.getReferenceById(id);
        Facility f = Facility.builder()
                .university(u)
                .nameEn((String) body.get("nameEn"))
                .nameKh((String) body.get("nameKh"))
                .iconKey((String) body.get("iconKey"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();
        facilityRepository.save(f);
        return ResponseEntity.status(201).body(Map.of("id", f.getId(), "message", "Facility added"));
    }

    @PutMapping("/{id}/facilities/{facilityId}")
    public ResponseEntity<?> updateFacility(@PathVariable UUID id, @PathVariable UUID facilityId,
                                            @RequestBody Map<String, Object> body) {
        Facility f = facilityRepository.findById(facilityId)
                .orElseThrow(() -> new NoSuchElementException("Facility not found"));
        if (body.containsKey("nameEn"))       f.setNameEn((String) body.get("nameEn"));
        if (body.containsKey("nameKh"))       f.setNameKh((String) body.get("nameKh"));
        if (body.containsKey("iconKey"))      f.setIconKey((String) body.get("iconKey"));
        if (body.containsKey("displayOrder")) f.setDisplayOrder((Integer) body.get("displayOrder"));
        facilityRepository.save(f);
        return ResponseEntity.ok(Map.of("message", "Facility updated"));
    }

    @DeleteMapping("/{id}/facilities/{facilityId}")
    public ResponseEntity<?> deleteFacility(@PathVariable UUID id, @PathVariable UUID facilityId) {
        facilityRepository.deleteById(facilityId);
        return ResponseEntity.ok(Map.of("message", "Facility deleted"));
    }

    // ── FACILITY PHOTOS ───────────────────────────────────────────────────────

    @PostMapping(value = "/{id}/facility-photos/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFacilityPhoto(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "altTextEn", required = false) String altTextEn,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder) {

        University u = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found"));

        String photoUrl = storageService.uploadFacilityPhoto(id, file);

        FacilityPhoto photo = FacilityPhoto.builder()
                .university(u)
                .photoUrl(photoUrl)
                .altTextEn(altTextEn)
                .displayOrder(displayOrder)
                .build();
        facilityPhotoRepository.save(photo);

        return ResponseEntity.status(201).body(Map.of(
                "id", photo.getId(), "photoUrl", photoUrl, "message", "Facility photo uploaded"));
    }

    @DeleteMapping("/{id}/facility-photos/{photoId}")
    public ResponseEntity<?> deleteFacilityPhoto(@PathVariable UUID id, @PathVariable UUID photoId) {
        facilityPhotoRepository.findById(photoId).ifPresent(p -> {
            storageService.deleteByUrl(p.getPhotoUrl());
            facilityPhotoRepository.delete(p);
        });
        return ResponseEntity.ok(Map.of("message", "Facility photo deleted"));
    }

    // ── UNIVERSITY MAJOR program details ──────────────────────────────────────

    @PatchMapping("/{id}/university-majors/{umId}")
    public ResponseEntity<?> updateUniversityMajor(
            @PathVariable UUID id, @PathVariable UUID umId,
            @RequestBody Map<String, Object> body) {

        UniversityMajor um = universityMajorRepository.findById(umId)
                .orElseThrow(() -> new NoSuchElementException("UniversityMajor not found"));

        if (body.containsKey("department"))          um.setDepartment((String) body.get("department"));
        if (body.containsKey("degreeType"))          um.setDegreeType((String) body.get("degreeType"));
        if (body.containsKey("credits"))             um.setCredits((Integer) body.get("credits"));
        if (body.containsKey("language"))            um.setLanguage((String) body.get("language"));
        if (body.containsKey("internshipRequired"))  um.setInternshipRequired((Boolean) body.get("internshipRequired"));
        if (body.containsKey("durationYears"))       um.setDurationYears((Integer) body.get("durationYears"));

        universityMajorRepository.save(um);
        return ResponseEntity.ok(Map.of("message", "UniversityMajor updated"));
    }

    @PostMapping("/{id}/university-majors/{umId}/prospects")
    public ResponseEntity<?> addCareerProspect(
            @PathVariable UUID id, @PathVariable UUID umId,
            @RequestBody Map<String, Object> body) {

        UniversityMajor um = universityMajorRepository.getReferenceById(umId);
        UniversityMajorCareerProspect prospect = UniversityMajorCareerProspect.builder()
                .universityMajor(um)
                .titleEn((String) body.get("titleEn"))
                .titleKh((String) body.get("titleKh"))
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .build();
        careerProspectRepository.save(prospect);
        return ResponseEntity.status(201).body(Map.of("id", prospect.getId(), "message", "Career prospect added"));
    }

    @DeleteMapping("/{id}/university-majors/{umId}/prospects/{prospectId}")
    public ResponseEntity<?> deleteCareerProspect(
            @PathVariable UUID id, @PathVariable UUID umId, @PathVariable UUID prospectId) {
        careerProspectRepository.deleteById(prospectId);
        return ResponseEntity.ok(Map.of("message", "Career prospect deleted"));
    }
}