package com.sakollife.entity;

import com.sakollife.entity.enums.CareerCategory;
import com.sakollife.entity.enums.JobOutlook;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Seeded with the 9 technology majors.
 * RIASEC fields store the major's profile vector (weighted H/M/L values).
 * careerCategory  — used as a filter chip on the results page
 * jobOutlook      — HIGH / MEDIUM / LOW demand in job market
 */
@Entity
@Table(name = "majors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String code; // CS, AI, DS, MWD, CYB, NET, DD, DB, MIS

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    // Filter fields

    /**
     * Career category — maps to a filter chip on the Majors results page.
     * e.g. SOFTWARE_ENGINEERING, ARTIFICIAL_INTELLIGENCE, DATA_ANALYTICS …
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "career_category", length = 50)
    private CareerCategory careerCategory;

    /**
     * Job market demand level for this major.
     * HIGH / MEDIUM / LOW — shown as a badge on the major card.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "job_outlook", length = 10)
    private JobOutlook jobOutlook;

    // RIASEC vector

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

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}