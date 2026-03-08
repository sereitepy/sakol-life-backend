package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Stores every quiz attempt (for authenticated users).
 * user_id is nullable to support temporary guest attempts before merge.
 * The Student Vector (vec_r through vec_c) is persisted here and is the
 * source of truth for cosine similarity calculations.
 */
@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Nullable, guest attempts don't have a user yet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    // Increments per user: 1, 2, 3...
    // Guest attempts that get merged become attempt_number = 1
    @Column(name = "attempt_number")
    private Integer attemptNumber;

    // Computed from the weighted RIASEC scoring algorithm.
    // These 6 values are compared against each Major's vector
    // using cosine similarity to produce the ranked recommendations.
    @Column(name = "vec_r", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecR;

    @Column(name = "vec_i", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecI;

    @Column(name = "vec_a", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecA;

    @Column(name = "vec_s", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecS;

    @Column(name = "vec_e", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecE;

    @Column(name = "vec_c", nullable = false, precision = 8, scale = 4)
    private BigDecimal vecC;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
