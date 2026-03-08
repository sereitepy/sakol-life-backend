package com.sakollife.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Payload sent from the frontend when a user (or guest) submits the quiz.
 *
 * Likert fields (Q2, Q7, Q8, Q9, Q11, Q13): integer 1–5
 * Single-choice fields: letter string "A", "B", "C"...
 * Q4 sub-questions (a–g): Likert 1–5 each
 *
 * guestAttemptId: only set when a registered user is merging a guest attempt.
 *                 Leave null for fresh submissions.
 */
@Data
public class QuizSubmitRequest {

    // Q1 — Single choice (A–D)
    @NotBlank
    private String q1;

    // Q2 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q2;

    // Q3 — Single choice (A–E)
    @NotBlank
    private String q3;

    // Q4 — Multi-Likert sub-questions (a–g), each 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q4A;

    @NotNull @Min(1) @Max(5)
    private Integer q4B;

    @NotNull @Min(1) @Max(5)
    private Integer q4C;

    @NotNull @Min(1) @Max(5)
    private Integer q4D;

    @NotNull @Min(1) @Max(5)
    private Integer q4E;

    @NotNull @Min(1) @Max(5)
    private Integer q4F;

    @NotNull @Min(1) @Max(5)
    private Integer q4G;

    // Q5 — Single choice (A–E)
    @NotBlank
    private String q5;

    // Q6 — Single choice (A–E)
    @NotBlank
    private String q6;

    // Q7 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q7;

    // Q8 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q8;

    // Q9 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q9;

    // Q10 — Single choice (A–E)
    @NotBlank
    private String q10;

    // Q11 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q11;

    // Q12 — Single choice (A–H)
    @NotBlank
    private String q12;

    // Q13 — Likert 1–5
    @NotNull @Min(1) @Max(5)
    private Integer q13;

    // Q14 — Single choice (A–H)
    @NotBlank
    private String q14;
}
