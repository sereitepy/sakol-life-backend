package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/** One "What You'll Study" subject card for a major. */
@Entity
@Table(name = "major_subjects")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MajorSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "name_en", nullable = false, length = 200)
    private String nameEn;

    @Column(name = "name_kh", length = 200)
    private String nameKh;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    /**
     * Icon identifier passed to the frontend icon system.
     * e.g. "code", "database", "shield", "cpu", "bar-chart"
     */
    @Column(name = "icon_key", length = 50)
    private String iconKey;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}