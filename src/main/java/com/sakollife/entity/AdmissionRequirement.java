package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "admission_requirements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdmissionRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @Column(name = "title_en", nullable = false, length = 200)
    private String titleEn;

    @Column(name = "title_kh", length = 200)
    private String titleKh;

    @Column(name = "description_en", columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(name = "description_kh", columnDefinition = "TEXT")
    private String descriptionKh;

    /** Optional CTA link — e.g. "View Exam Schedule →" */
    @Column(name = "link_label_en", length = 100)
    private String linkLabelEn;

    @Column(name = "link_url", columnDefinition = "TEXT")
    private String linkUrl;

    /** Icon key for frontend — "diploma", "star", "clipboard" */
    @Column(name = "icon_key", length = 50)
    private String iconKey;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}