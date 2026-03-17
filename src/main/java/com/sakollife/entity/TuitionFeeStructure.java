package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "tuition_fee_structures")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TuitionFeeStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "program_type_en", nullable = false, length = 150)
    private String programTypeEn;

    @Column(name = "program_type_kh", length = 150)
    private String programTypeKh;

    @Column(name = "fee_per_semester", precision = 10, scale = 2)
    private BigDecimal feePerSemester;

    @Column(name = "fee_per_year", precision = 10, scale = 2)
    private BigDecimal feePerYear;

    @Column(name = "notes_en", length = 300)
    private String notesEn;

    @Column(name = "notes_kh", length = 300)
    private String notesKh;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}