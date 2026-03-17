package com.sakollife.entity;

import com.sakollife.entity.enums.UniversityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "universities")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "location_city", nullable = false)
    private String locationCity;

    @Column(name = "logo_url")
    private String logoUrl;

    /** Full-width hero banner — uploaded to DO Spaces by admin */
    @Column(name = "banner_url", columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UniversityType type;

    // ── Overview section ──────────────────────────────────────────────────────

    @Column(name = "established_date")
    private LocalDate establishedDate;

    @Column(name = "accreditation", length = 200)
    private String accreditation;

    @Column(name = "overview_en", columnDefinition = "TEXT")
    private String overviewEn;

    @Column(name = "overview_kh", columnDefinition = "TEXT")
    private String overviewKh;

    // ── Child collections (lazy — only loaded in detail endpoint) ─────────────

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<AdmissionRequirement> admissionRequirements = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<TuitionFeeStructure> tuitionFees = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Scholarship> scholarships = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<Facility> facilities = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<FacilityPhoto> facilityPhotos = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}