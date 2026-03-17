package com.sakollife.controller;

import com.sakollife.entity.*;
import com.sakollife.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * GET /api/v1/universities/{id}/detail
 *
 * Powers the individual University detail page with all sections:
 *  - Hero banner + overview stats
 *  - Selected major card (if user has a selected major offered by this university)
 *  - Admission requirements
 *  - Tuition & fees table
 *  - Scholarships
 *  - Campus facilities (icon list + photo grid)
 */
@RestController
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
public class UniversityDetailController {

    private final UniversityRepository universityRepository;
    private final UniversityMajorRepository universityMajorRepository;
    private final UniversityMajorCareerProspectRepository careerProspectRepository;
    private final AdmissionRequirementRepository admissionRequirementRepository;
    private final TuitionFeeStructureRepository tuitionFeeStructureRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final FacilityRepository facilityRepository;
    private final FacilityPhotoRepository facilityPhotoRepository;
    private final ProfileRepository profileRepository;
    private final GuestMajorSelectionRepository guestMajorSelectionRepository;

    /**
     * GET /api/v1/universities/{id}/detail
     *
     * Optional headers for personalisation:
     *   Authorization: Bearer <jwt>          — registered user, loads selectedMajor from profile
     *   X-Guest-Session-Id: <uuid>           — guest, loads selectedMajor from guest_major_selections
     *
     * Response:
     * {
     *   "universityId": "uuid",
     *   "nameEn": "Royal University of Phnom Penh",
     *   "nameKh": "...",
     *   "type": "PUBLIC",
     *   "locationCity": "Phnom Penh",
     *   "logoUrl": "...",
     *   "bannerUrl": "...",
     *   "websiteUrl": "...",
     *   "establishedDate": "1960-01-13",
     *   "accreditation": "ACC Accredited",
     *   "overviewEn": "...",
     *   "overviewKh": "...",
     *   "selectedMajorProgram": {             // null if user has no selected major OR
     *     "universityMajorId": "uuid",        // this university doesn't offer it
     *     "majorId": "uuid",
     *     "majorCode": "CS",
     *     "majorNameEn": "Computer Science",
     *     "department": "Department of Computer Science",
     *     "degreeType": "Bachelor of Science",
     *     "durationYears": 4,
     *     "credits": 128,
     *     "language": "Eng / Khmer",
     *     "internshipRequired": true,
     *     "careerProspects": ["Software Engineer", "Data Analyst", ...]
     *   },
     *   "admissionRequirements": [ ... ],
     *   "tuitionFees": [ ... ],
     *   "scholarships": [ ... ],
     *   "facilities": [ ... ],
     *   "facilityPhotos": [ ... ]
     * }
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<?> getUniversityDetail(
            @PathVariable UUID id,
            Authentication authentication,
            @RequestHeader(value = "X-Guest-Session-Id", required = false) UUID guestSessionId) {

        University university = universityRepository.findById(id).orElse(null);
        if (university == null) return ResponseEntity.notFound().build();

        // Resolve the user's currently selected major
        Major selectedMajor = null;
        if (authentication != null && authentication.isAuthenticated()) {
            UUID userId = (UUID) authentication.getPrincipal();
            selectedMajor = profileRepository.findById(userId)
                    .map(Profile::getSelectedMajor).orElse(null);
        } else if (guestSessionId != null) {
            selectedMajor = guestMajorSelectionRepository
                    .findByGuestSessionId(guestSessionId)
                    .map(GuestMajorSelection::getMajor).orElse(null);
        }

        // Selected major program — this university's offering of the selected major
        Map<String, Object> selectedMajorProgram = null;
        if (selectedMajor != null) {
            final UUID majorId = selectedMajor.getId();
            var umOpt = universityMajorRepository
                    .findByMajorWithFilters(id, null, null, null, null)
                    .stream()
                    .filter(um -> um.getMajor().getId().equals(majorId))
                    .findFirst();

            if (umOpt.isPresent()) {
                UniversityMajor um = umOpt.get();
                List<String> prospects = careerProspectRepository
                        .findByUniversityMajorIdOrderByDisplayOrderAsc(um.getId())
                        .stream().map(UniversityMajorCareerProspect::getTitleEn).toList();

                selectedMajorProgram = new LinkedHashMap<>();
                selectedMajorProgram.put("universityMajorId",  um.getId());
                selectedMajorProgram.put("majorId",            um.getMajor().getId());
                selectedMajorProgram.put("majorCode",          um.getMajor().getCode());
                selectedMajorProgram.put("majorNameEn",        um.getMajor().getNameEn());
                selectedMajorProgram.put("majorNameKh",        um.getMajor().getNameKh());
                selectedMajorProgram.put("department",         um.getDepartment());
                selectedMajorProgram.put("degreeType",         um.getDegreeType() != null
                        ? um.getDegreeType() : um.getMajor().getDegreeType());
                selectedMajorProgram.put("durationYears",      um.getDurationYears());
                selectedMajorProgram.put("credits",            um.getCredits());
                selectedMajorProgram.put("language",           um.getLanguage() != null
                        ? um.getLanguage() : um.getMajor().getLanguage());
                selectedMajorProgram.put("internshipRequired", um.getInternshipRequired());
                selectedMajorProgram.put("careerProspects",    prospects);
            }
        }

        // Admission requirements
        List<Map<String, Object>> admissionRequirements = admissionRequirementRepository
                .findByUniversityIdOrderByDisplayOrderAsc(id).stream()
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId()); m.put("titleEn", a.getTitleEn()); m.put("titleKh", a.getTitleKh());
                    m.put("descriptionEn", a.getDescriptionEn()); m.put("descriptionKh", a.getDescriptionKh());
                    m.put("linkLabelEn", a.getLinkLabelEn()); m.put("linkUrl", a.getLinkUrl());
                    m.put("iconKey", a.getIconKey()); m.put("displayOrder", a.getDisplayOrder());
                    return m;
                }).toList();

        // Tuition fee table
        List<Map<String, Object>> tuitionFees = tuitionFeeStructureRepository
                .findByUniversityIdOrderByDisplayOrderAsc(id).stream()
                .map(t -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", t.getId()); m.put("programTypeEn", t.getProgramTypeEn());
                    m.put("programTypeKh", t.getProgramTypeKh());
                    m.put("feePerSemester", t.getFeePerSemester()); m.put("feePerYear", t.getFeePerYear());
                    m.put("notesEn", t.getNotesEn()); m.put("notesKh", t.getNotesKh());
                    m.put("displayOrder", t.getDisplayOrder());
                    return m;
                }).toList();

        // Scholarships
        List<Map<String, Object>> scholarships = scholarshipRepository
                .findByUniversityId(id).stream()
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId()); m.put("nameEn", s.getNameEn()); m.put("nameKh", s.getNameKh());
                    m.put("coverageLabel", s.getCoverageLabel());
                    m.put("descriptionEn", s.getDescriptionEn()); m.put("descriptionKh", s.getDescriptionKh());
                    m.put("deadline", s.getDeadline());
                    return m;
                }).toList();

        // Facilities icon list
        List<Map<String, Object>> facilities = facilityRepository
                .findByUniversityIdOrderByDisplayOrderAsc(id).stream()
                .map(f -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", f.getId()); m.put("nameEn", f.getNameEn()); m.put("nameKh", f.getNameKh());
                    m.put("iconKey", f.getIconKey()); m.put("displayOrder", f.getDisplayOrder());
                    return m;
                }).toList();

        // Facility photo grid
        List<Map<String, Object>> facilityPhotos = facilityPhotoRepository
                .findByUniversityIdOrderByDisplayOrderAsc(id).stream()
                .map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", p.getId()); m.put("photoUrl", p.getPhotoUrl());
                    m.put("altTextEn", p.getAltTextEn()); m.put("displayOrder", p.getDisplayOrder());
                    return m;
                }).toList();

        // Build final response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("universityId",          university.getId());
        response.put("nameEn",                university.getNameEn());
        response.put("nameKh",                university.getNameKh());
        response.put("type",                  university.getType());
        response.put("locationCity",          university.getLocationCity());
        response.put("logoUrl",               university.getLogoUrl());
        response.put("bannerUrl",             university.getBannerUrl());
        response.put("websiteUrl",            university.getWebsiteUrl());
        response.put("establishedDate",       university.getEstablishedDate());
        response.put("accreditation",         university.getAccreditation());
        response.put("overviewEn",            university.getOverviewEn());
        response.put("overviewKh",            university.getOverviewKh());
        response.put("selectedMajorProgram",  selectedMajorProgram);
        response.put("admissionRequirements", admissionRequirements);
        response.put("tuitionFees",           tuitionFees);
        response.put("scholarships",          scholarships);
        response.put("facilities",            facilities);
        response.put("facilityPhotos",        facilityPhotos);

        return ResponseEntity.ok(response);
    }
}