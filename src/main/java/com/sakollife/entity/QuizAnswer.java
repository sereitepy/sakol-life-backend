package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_answers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt attempt;

    // e.g. "Q1", "Q4_A", "Q4_B", "Q12", "Q14"
    @Column(name = "question_code", nullable = false, length = 10)
    private String questionCode;

    // Selected answer: option letter ("A","B"...) or likert integer ("1" to "5")
    @Column(name = "answer_value", nullable = false, length = 10)
    private String answerValue;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
