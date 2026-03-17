package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "scholarships")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Scholarship {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "name_kh", length = 200)
    private String nameKh;

    /**
     * Badge label — shown as a coloured chip on the scholarship card.
     * e.g. "100% Tuition", "50% Grant"
     */
    @Column(name = "coverage_label", length = 50)
    private String coverageLabel;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    @Column(name = "deadline")
    private LocalDate deadline;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}