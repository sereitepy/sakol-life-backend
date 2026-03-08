package com.sakollife.util;

import org.springframework.stereotype.Component;

/**
 * Computes cosine similarity between two RIASEC vectors.
 *
 * Formula: cosine_similarity(A, B) = (A · B) / (|A| × |B|)
 * Returns a value between 0.0 and 1.0.
 * Multiply by 100 to get percentage for display.
 */
@Component
public class CosineSimilarityCalculator {

    /**
     * @param studentVector double[6] — [R, I, A, S, E, C] for the student
     * @param majorVector   double[6] — [R, I, A, S, E, C] for the major
     * @return similarity score between 0.0 and 1.0
     */
    public double calculate(double[] studentVector, double[] majorVector) {
        if (studentVector.length != 6 || majorVector.length != 6) {
            throw new IllegalArgumentException("RIASEC vectors must have exactly 6 dimensions");
        }

        double dotProduct = 0.0;
        double studentMagnitude = 0.0;
        double majorMagnitude = 0.0;

        for (int i = 0; i < 6; i++) {
            dotProduct += studentVector[i] * majorVector[i];
            studentMagnitude += Math.pow(studentVector[i], 2);
            majorMagnitude += Math.pow(majorVector[i], 2);
        }

        studentMagnitude = Math.sqrt(studentMagnitude);
        majorMagnitude = Math.sqrt(majorMagnitude);

        // Avoid division by zero
        if (studentMagnitude == 0 || majorMagnitude == 0) {
            return 0.0;
        }

        return dotProduct / (studentMagnitude * majorMagnitude);
    }
}
