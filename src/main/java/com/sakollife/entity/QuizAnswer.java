package com.sakollife.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Stores the raw selected answers for the LATEST quiz attempt only.
 * Purpose: display-only, shows the user their previously selected answers
 * in the profile dashboard dialog.
 *
 * On every new quiz submission by an authenticated user:
 * 1. All existing QuizAnswer rows for that user are DELETED
 * 2. New rows for the latest attempt are INSERTED
 *
 * question_code examples: "Q1", "Q2", "Q3", "Q4_A", "Q4_B", ..., "Q14"
 * answer_value examples:  "A", "B", "3", "5" (option letter or likert integer)
 */
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
