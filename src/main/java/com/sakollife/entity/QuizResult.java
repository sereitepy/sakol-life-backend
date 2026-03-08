package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Stores cosine similarity score between a quiz attempt and each of the 9 majors.
 * One attempt produces exactly 9 rows here (one per major).
 * similarity_score: 0.0000 to 1.0000 (multiply by 100 for percentage display)
 */
@Entity
@Table(name = "quiz_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    // 0.0000 to 1.0000
    @Column(name = "similarity_score", nullable = false, precision = 5, scale = 4)
    private BigDecimal similarityScore;

    // 1 = highest match, 9 = lowest match
    @Column(name = "rank", nullable = false)
    private Integer rank;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
