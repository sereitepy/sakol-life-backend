package com.sakollife.service.impl;

import com.sakollife.entity.AnswerOption;
import com.sakollife.entity.Question;
import com.sakollife.entity.enums.QuestionFormat;
import com.sakollife.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Data-driven replacement for the hardcoded VectorCalculator.
 *
 * Builds the 6-dimensional RIASEC Student Vector from quiz answers
 * by loading question definitions, weights, and RIASEC mappings from the database.
 *
 * Vector index: [0]=R, [1]=I, [2]=A, [3]=S, [4]=E, [5]=C
 *
 * For SINGLE_CHOICE questions:
 *   contribution = answerOption.scoreValue × question.weight × answerOption.riasec[dim]
 *
 * For LIKERT questions (1–5):
 *   contribution = likertValue × question.weight × question.likert[dim]
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorService {

    private final QuestionRepository questionRepository;

    /**
     * Calculates the Student Vector from a map of question answers.
     *
     * @param answers Map of questionCode → answerValue
     *                e.g. {"Q1" → "A", "Q2" → "3", "Q4_A" → "5", ...}
     * @return double[6] RIASEC vector [R, I, A, S, E, C]
     */
    public double[] calculate(Map<String, String> answers) {
        double[] vector = new double[6];

        List<Question> questions = questionRepository.findAllActiveWithOptions();

        if (questions.isEmpty()) {
            throw new IllegalStateException("No active questions found in database. Please run the seed script.");
        }

        // Build a lookup map: questionCode -> Question
        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getQuestionCode, q -> q));

        for (Map.Entry<String, String> entry : answers.entrySet()) {
            String questionCode = entry.getKey();
            String answerValue = entry.getValue();

            Question question = questionMap.get(questionCode);
            if (question == null) {
                log.warn("Answer submitted for unknown question code: {}. Skipping.", questionCode);
                continue;
            }

            double weight = question.getWeight().doubleValue();

            if (question.getFormat() == QuestionFormat.LIKERT) {
                // Likert: integer 1-5 multiplied by weight and question-level RIASEC flags
                int likertValue = parseLikert(answerValue, questionCode);
                addLikertContribution(vector, likertValue, weight, question);

            } else {
                // Single choice: find the matching AnswerOption and apply its RIASEC contributions
                AnswerOption option = question.getOptions().stream()
                        .filter(o -> o.getOptionLetter().equalsIgnoreCase(answerValue))
                        .findFirst()
                        .orElse(null);

                if (option == null) {
                    log.warn("No answer option '{}' found for question '{}'. Skipping.", answerValue, questionCode);
                    continue;
                }

                addOptionContribution(vector, option, weight);
            }
        }

        log.debug("Computed Student Vector: R={} I={} A={} S={} E={} C={}",
                String.format("%.4f", vector[0]), String.format("%.4f", vector[1]),
                String.format("%.4f", vector[2]), String.format("%.4f", vector[3]),
                String.format("%.4f", vector[4]), String.format("%.4f", vector[5]));

        return vector;
    }


    /**
     * For SINGLE_CHOICE: adds contribution from an answer option.
     * contribution[dim] = scoreValue × weight × riasecFlag[dim]
     */
    private void addOptionContribution(double[] vector, AnswerOption option, double weight) {
        double score = option.getScoreValue();
        vector[0] += score * weight * option.getRiasecR().doubleValue();
        vector[1] += score * weight * option.getRiasecI().doubleValue();
        vector[2] += score * weight * option.getRiasecA().doubleValue();
        vector[3] += score * weight * option.getRiasecS().doubleValue();
        vector[4] += score * weight * option.getRiasecE().doubleValue();
        vector[5] += score * weight * option.getRiasecC().doubleValue();
    }

    /**
     * For LIKERT: adds contribution based on question-level RIASEC flags.
     * contribution[dim] = likertValue × weight × likertFlag[dim]
     */
    private void addLikertContribution(double[] vector, int likertValue, double weight, Question question) {
        vector[0] += likertValue * weight * question.getLikertR().doubleValue();
        vector[1] += likertValue * weight * question.getLikertI().doubleValue();
        vector[2] += likertValue * weight * question.getLikertA().doubleValue();
        vector[3] += likertValue * weight * question.getLikertS().doubleValue();
        vector[4] += likertValue * weight * question.getLikertE().doubleValue();
        vector[5] += likertValue * weight * question.getLikertC().doubleValue();
    }

    private int parseLikert(String value, String questionCode) {
        try {
            int v = Integer.parseInt(value.trim());
            if (v < 1 || v > 5) throw new IllegalArgumentException("Out of range");
            return v;
        } catch (Exception e) {
            log.warn("Invalid likert value '{}' for question '{}'. Defaulting to 1.", value, questionCode);
            return 1;
        }
    }
}
