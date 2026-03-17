package com.sakollife.controller.admin;

import com.sakollife.entity.University;
import com.sakollife.repository.UniversityRepository;
import com.sakollife.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/universities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUniversityController {

    private final UniversityRepository universityRepository;
    private final StorageService storageService;

    /**
     * POST /api/v1/admin/universities/{id}/upload-banner
     **
     * Content-Type: multipart/form-data
     * Form field: "file" — the image (jpg/png/webp, max 5 MB)
     *
     * Response 200:
     * {
     *   "universityId": "uuid",
     *   "bannerUrl": "https://<space>.cdn.digitaloceanspaces.com/university-banners/<uuid>.jpg"
     * }
     */
    @PostMapping(
            value = "/{id}/upload-banner",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadBanner(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {

        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found: " + id));

        String bannerUrl = storageService.uploadUniversityBanner(id, file);

        university.setBannerUrl(bannerUrl);
        universityRepository.save(university);

        return ResponseEntity.ok(Map.of(
                "universityId", university.getId(),
                "bannerUrl",    bannerUrl
        ));
    }

    /**
     * DELETE /api/v1/admin/universities/{id}/banner
     *
     * Removes the banner URL from the university and deletes the file from DO Spaces.
     * Response 200: { "message": "Banner removed" }
     */
    @DeleteMapping("/{id}/banner")
    public ResponseEntity<?> deleteBanner(@PathVariable UUID id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("University not found: " + id));

        if (university.getBannerUrl() != null) {
            storageService.deleteByUrl(university.getBannerUrl());
            university.setBannerUrl(null);
            universityRepository.save(university);
        }

        return ResponseEntity.ok(Map.of("message", "Banner removed"));
    }
}