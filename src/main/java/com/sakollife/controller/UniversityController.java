package com.sakollife.controller;

import com.sakollife.entity.UniversityMajor;
import com.sakollife.entity.enums.UniversityType;
import com.sakollife.repository.UniversityMajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/universities")
@RequiredArgsConstructor
public class UniversityController {

    private final UniversityMajorRepository universityMajorRepository;

//    GET /api/v1/universities?majorId=&type=&city=&maxFee=&durationYears=


    @GetMapping
    public ResponseEntity<?> getUniversitiesByMajor(
            @RequestParam UUID majorId,
            @RequestParam(required = false) UniversityType type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) Integer durationYears) {

        List<UniversityMajor> results = universityMajorRepository
                .findByMajorWithFilters(majorId, type, city, maxFee, durationYears);

        List<?> response = results.stream().map(um -> new java.util.LinkedHashMap<String, Object>() {{
            put("universityId", um.getUniversity().getId());
            put("nameEn", um.getUniversity().getNameEn());
            put("nameKh", um.getUniversity().getNameKh());
            put("locationCity", um.getUniversity().getLocationCity());
            put("logoUrl", um.getUniversity().getLogoUrl());
            put("websiteUrl", um.getUniversity().getWebsiteUrl());
            put("type", um.getUniversity().getType());
            put("tuitionFeeUsd", um.getTuitionFeeUsd());
            put("durationYears", um.getDurationYears());
        }}).toList();

        return ResponseEntity.ok(response);
    }
}
