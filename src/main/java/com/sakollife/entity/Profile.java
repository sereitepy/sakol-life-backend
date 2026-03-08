package com.sakollife.entity;

import com.sakollife.entity.enums.Language;
import com.sakollife.entity.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", nullable = false)
    @Builder.Default
    private Language preferredLanguage = Language.EN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    /**
     * The major the user has currently selected on their results page.
     * Nullable. null means no major is selected and the University tab is unavailable.
     * Set when the user clicks a major card.
     * Cleared when the user clicks X on the selected major.
     *
     * Stored as selected_major_id (FK → majors.id) on the profiles table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_major_id", nullable = true)
    private Major selectedMajor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
