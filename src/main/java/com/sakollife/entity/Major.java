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

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    @Column(name = "faculty", length = 200)
    private String faculty;

    @Column(name = "degree_type", length = 100)
    private String degreeType;

    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "icon_url", columnDefinition = "TEXT")
    private String iconUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_category", length = 50)
    private CareerCategory careerCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_outlook", length = 10)
    private JobOutlook jobOutlook;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_demand_level", length = 20)
    private JobDemandLevel jobDemandLevel;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}