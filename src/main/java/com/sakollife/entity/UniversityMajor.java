package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "university_majors",
        uniqueConstraints = @UniqueConstraint(columnNames = {"university_id", "major_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UniversityMajor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;


    @Column(name = "tuition_fee_usd", precision = 10, scale = 2)
    private BigDecimal tuitionFeeUsd;

    @Column(name = "duration_years")
    private Integer durationYears;


    @Column(name = "department", length = 200)
    private String department;

    @Column(name = "degree_type", length = 100)
    private String degreeType;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "language", length = 100)
    private String language;

    @Column(name = "internship_required")
    @Builder.Default
    private Boolean internshipRequired = false;

    @OneToMany(mappedBy = "universityMajor", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<UniversityMajorCareerProspect> careerProspects = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}