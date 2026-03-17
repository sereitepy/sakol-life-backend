package com.sakollife.entity;

import com.sakollife.entity.enums.UniversityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "universities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_kh", nullable = false)
    private String nameKh;

    @Column(name = "location_city", nullable = false)
    private String locationCity;

    /**
     * Small logo — kept as a plain URL string (can be an external CDN link or DO Spaces URL).
     * Admin sets this manually.
     */
    @Column(name = "logo_url")
    private String logoUrl;

    /**
     * Full-width banner image, uploaded by admin to Digital Ocean Spaces.
     * Set via POST /api/v1/admin/universities/{id}/upload-banner
     * Stored as the public DO Spaces CDN URL.
     */
    @Column(name = "banner_url", columnDefinition = "TEXT")
    private String bannerUrl;

    @Column(name = "website_url")
    private String websiteUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UniversityType type;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}