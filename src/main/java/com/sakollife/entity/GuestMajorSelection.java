package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_major_selections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestMajorSelection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "guest_session_id", nullable = false)
    private UUID guestSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Column(name = "selected_at", nullable = false)
    @Builder.Default
    private OffsetDateTime selectedAt = OffsetDateTime.now();
}
