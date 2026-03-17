package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Profile user;

    @Column(name = "attempt_number")
    private Integer attemptNumber;

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
