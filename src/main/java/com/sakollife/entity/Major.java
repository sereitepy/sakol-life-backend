package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Seeded with the 9 technology majors.
 * RIASEC fields store the major's profile vector (weighted H/M/L values).
 * H = 1.0, M = 0.5, L = 0.1 — set these during seeding.
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

    // RIASEC vector values for this major
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
