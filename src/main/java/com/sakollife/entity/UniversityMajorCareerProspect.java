package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "university_major_career_prospects")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UniversityMajorCareerProspect {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_major_id", nullable = false)
    private UniversityMajor universityMajor;

    @Column(name = "title_en", nullable = false, length = 150)
    private String titleEn;

    @Column(name = "title_kh", length = 150)
    private String titleKh;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}