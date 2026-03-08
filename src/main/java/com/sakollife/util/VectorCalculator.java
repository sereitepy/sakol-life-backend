package com.sakollife.util;

import com.sakollife.dto.request.QuizSubmitRequest;
import org.springframework.stereotype.Component;

/**
 * Converts raw quiz answers into a weighted 6-dimensional RIASEC Student Vector.
 *
 * RIASEC index mapping: [0]=R, [1]=I, [2]=A, [3]=S, [4]=E, [5]=C
 *
 * Weight levels:
 *   1.0 — general confidence / feelings questions
 *   1.5 — preferred tasks / work style questions
 *   2.0 — specific career motivations and intentions
 *
 * Single-choice answers (A/B/C...) are converted to a score (1–5 scale equivalent)
 * based on their position, then applied to their RIASEC dimension(s).
 * Likert answers (1–5) are used directly.
 */
@Component
public class VectorCalculator {

    // Index constants for readability
    private static final int R = 0, I = 1, A = 2, S = 3, E = 4, C = 5;

    /**
     * Builds the Student Vector from a completed quiz submission.
     * @return double[6] representing [R, I, A, S, E, C] weighted scores
     */
    public double[] calculate(QuizSubmitRequest req) {
        double[] vector = new double[6];

        // ── Q1: Technology familiarity — Weight 1.0 — Dimensions: I, R ──
        // A=4 (high familiarity→I,R), B=3, C=2, D=1
        int q1Score = letterToScore(req.getQ1(), 4); // 4 options, descending
        addToVector(vector, q1Score, 1.0, I, R);

        // ── Q2: Troubleshooting confidence — Likert 1-5 — Weight 1.0 — Dimensions: R, I ──
        addToVector(vector, req.getQ2(), 1.0, R, I);

        // ── Q3: Preferred task type — Weight 1.5 — Dimensions: R, I, A, E, C ──
        // A=Designing(A), B=Analyzing(I,C), C=Security(R,I), D=Interactive(A,R), E=Business(E,C)
        applyQ3(vector, req.getQ3());

        // ── Q4: Interest in 7 tech areas — Multi-Likert 1-5 — Weight 2.0 ──
        // Q4a: Programming → R, I
        addToVector(vector, req.getQ4A(), 2.0, R, I);
        // Q4b: AI/ML → I, A
        addToVector(vector, req.getQ4B(), 2.0, I, A);
        // Q4c: Data analysis → I, C
        addToVector(vector, req.getQ4C(), 2.0, I, C);
        // Q4d: Cybersecurity → R, I, C
        addToVector(vector, req.getQ4D(), 2.0, R, I, C);
        // Q4e: Networks → R, C
        addToVector(vector, req.getQ4E(), 2.0, R, C);
        // Q4f: Web design / multimedia → A, R
        addToVector(vector, req.getQ4F(), 2.0, A, R);
        // Q4g: Tech in business → E, S, C
        addToVector(vector, req.getQ4G(), 2.0, E, S, C);

        // ── Q5: Self-directed learning — Weight 1.0 — Dimensions: I, R ──
        // A=1(very poor) → E=5(excellent)
        int q5Score = letterToAscendingScore(req.getQ5(), 5);
        addToVector(vector, q5Score, 1.0, I, R);

        // ── Q6: Preferred work environment — Weight 1.5 — Dimensions: S, I, C, A, E ──
        applyQ6(vector, req.getQ6());

        // ── Q7: Logic/analytical enjoyment — Likert 1-5 — Weight 1.5 — Dimensions: I, R ──
        addToVector(vector, req.getQ7(), 1.5, I, R);

        // ── Q8: Visual creativity interest — Likert 1-5 — Weight 1.5 — Dimension: A ──
        addToVector(vector, req.getQ8(), 1.5, A);

        // ── Q9: Comfort presenting ideas — Likert 1-5 — Weight 1.0 — Dimensions: S, E ──
        addToVector(vector, req.getQ9(), 1.0, S, E);

        // ── Q10: Desired career outcome — Weight 2.0 — Dimensions: R, I, C, A, E ──
        applyQ10(vector, req.getQ10());

        // ── Q11: Creativity + technology importance — Likert 1-5 — Weight 1.0 — Dims: A, I ──
        addToVector(vector, req.getQ11(), 1.0, A, I);

        // ── Q12: Core motivation — Weight 2.0 — All 6 dimensions ──
        applyQ12(vector, req.getQ12());

        // ── Q13: Excitement about tech major — Likert 1-5 — Weight 1.0 — General boost ──
        // Distributes equally across all dimensions as a general enthusiasm signal
        double q13Contribution = req.getQ13() * 1.0 * 0.1; // small equal contribution
        for (int i = 0; i < 6; i++) vector[i] += q13Contribution;

        // ── Q14: Most curious tech areas — Weight 2.0 — All 6 dimensions ──
        applyQ14(vector, req.getQ14());

        return vector;
    }

    // ── Q3: Preferred task type ───────────────────────────────────────────────
    private void applyQ3(double[] v, String answer) {
        double w = 1.5;
        switch (answer.toUpperCase()) {
            case "A" -> addToVector(v, 4, w, A);           // Designing → Artistic
            case "B" -> addToVector(v, 4, w, I, C);        // Analyzing → Investigative, Conventional
            case "C" -> addToVector(v, 4, w, R, I);        // Security → Realistic, Investigative
            case "D" -> addToVector(v, 4, w, A, R);        // Interactive digital → Artistic, Realistic
            case "E" -> addToVector(v, 4, w, E, C);        // Business processes → Enterprising, Conventional
        }
    }

    // ── Q6: Preferred work environment ────────────────────────────────────────
    private void applyQ6(double[] v, String answer) {
        double w = 1.5;
        switch (answer.toUpperCase()) {
            case "A" -> addToVector(v, 4, w, S);           // Collaborative → Social
            case "B" -> addToVector(v, 4, w, I);           // Independent → Investigative
            case "C" -> addToVector(v, 4, w, R, I);        // Fast-paced problem solving → Realistic, Investigative
            case "D" -> addToVector(v, 4, w, A);           // Creative spaces → Artistic
            case "E" -> addToVector(v, 4, w, C, E);        // Structured business → Conventional, Enterprising
        }
    }

    // ── Q10: Desired career outcome ───────────────────────────────────────────
    private void applyQ10(double[] v, String answer) {
        double w = 2.0;
        switch (answer.toUpperCase()) {
            case "A" -> addToVector(v, 5, w, R, I);        // Building systems → Realistic, Investigative
            case "B" -> addToVector(v, 5, w, I, C);        // Data-driven decisions → Investigative, Conventional
            case "C" -> addToVector(v, 5, w, R, I, C);     // Security/compliance → Realistic, Investigative, Conventional
            case "D" -> addToVector(v, 5, w, A);           // Designing interfaces → Artistic
            case "E" -> addToVector(v, 5, w, E, C);        // Digital business ops → Enterprising, Conventional
        }
    }

    // ── Q12: Core motivation ──────────────────────────────────────────────────
    private void applyQ12(double[] v, String answer) {
        double w = 2.0;
        switch (answer.toUpperCase()) {
            case "A" -> addToVector(v, 5, w, R, I);        // Build apps/tools → R, I
            case "B" -> addToVector(v, 5, w, I);           // Understand tech deeply → I
            case "C" -> addToVector(v, 5, w, A);           // Creativity in digital experiences → A
            case "D" -> addToVector(v, 5, w, E, C);        // Grow business online → E, C
            case "E" -> addToVector(v, 5, w, R, I, C);     // Protect people/privacy → R, I, C
            case "F" -> addToVector(v, 5, w, I, C);        // Data-driven decisions → I, C
            case "G" -> addToVector(v, 5, w, E, S);        // Lead tech projects → E, S
            case "H" -> addToVector(v, 5, w, I, A);        // Cutting-edge/future tech → I, A
        }
    }

    // ── Q14: Most curious tech areas ──────────────────────────────────────────
    private void applyQ14(double[] v, String answer) {
        double w = 2.0;
        switch (answer.toUpperCase()) {
            case "A" -> addToVector(v, 5, w, R, I);        // Writing code/software → R, I
            case "B" -> addToVector(v, 5, w, I, A);        // AI/smart systems → I, A
            case "C" -> addToVector(v, 5, w, A, R);        // Design apps/websites → A, R
            case "D" -> addToVector(v, 5, w, E, C);        // Online business/marketing → E, C
            case "E" -> addToVector(v, 5, w, R, I, C);     // Cybersecurity/hacking → R, I, C
            case "F" -> addToVector(v, 5, w, R, C);        // Networks/infrastructure → R, C
            case "G" -> addToVector(v, 5, w, I, C);        // Data/trends → I, C
            case "H" -> addToVector(v, 5, w, E, S, C);     // Managing tech/teams → E, S, C
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Adds a weighted score contribution to specified RIASEC dimensions.
     */
    private void addToVector(double[] vector, int score, double weight, int... dimensions) {
        double contribution = score * weight / dimensions.length;
        for (int dim : dimensions) {
            vector[dim] += contribution;
        }
    }

    /**
     * Converts an answer letter (A, B, C...) to a descending score.
     * A = maxScore, B = maxScore-1, etc.
     * Used when A is the "best" or "highest" option.
     */
    private int letterToScore(String letter, int maxScore) {
        int index = letter.toUpperCase().charAt(0) - 'A';
        return Math.max(1, maxScore - index);
    }

    /**
     * Converts an answer letter to an ascending score.
     * A = 1, B = 2, C = 3... (used when A = worst, last = best)
     */
    private int letterToAscendingScore(String letter, int maxScore) {
        int index = letter.toUpperCase().charAt(0) - 'A';
        return Math.min(maxScore, index + 1);
    }
}
