package com.sakollife.entity;

import com.sakollife.entity.enums.CareerCategory;
import com.sakollife.entity.enums.JobDemandLevel;
import com.sakollife.entity.enums.JobOutlook;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "majors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    // ── Display ───────────────────────────────────────────────────────────────

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    /** "Faculty of Engineering & Technology" — chip above the major title */
    @Column(name = "faculty", length = 200)
    private String faculty;

    /** "Bachelor of Science" */
    @Column(name = "degree_type", length = 100)
    private String degreeType;

    /** "English / Khmer" */
    @Column(name = "language", length = 100)
    private String language;

    /** Small icon shown in Related Majors sidebar and major cards */
    @Column(name = "icon_url", columnDefinition = "TEXT")
    private String iconUrl;

    // ── Filter fields ─────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "career_category", length = 50)
    private CareerCategory careerCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_outlook", length = 10)
    private JobOutlook jobOutlook;

    // ── Job Market sidebar ────────────────────────────────────────────────────

    /** VERY_HIGH | HIGH | MEDIUM | LOW — drives the demand bar colour */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_demand_level", length = 20)
    private JobDemandLevel jobDemandLevel;

    /** Entry-level salary range, USD/month */
    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    // ── RIASEC vector ─────────────────────────────────────────────────────────

    @Column(name = "riasec_r", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecR;

    @Column(name = "riasec_i", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecI;

    @Column(name = "riasec_a", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecA;

    @Column(name = "riasec_s", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecS;

    @Column(name = "riasec_e", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecE;

    @Column(name = "riasec_c", nullable = false, precision = 4, scale = 2)
    private BigDecimal riasecC;

    // ── Child collections (lazy — only loaded in detail endpoint) ─────────────

    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MajorSubject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MajorSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "major", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MajorCareerOpportunity> careerOpportunities = new ArrayList<>();

    // ── Timestamps ────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}