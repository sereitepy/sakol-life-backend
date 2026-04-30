package com.sakollife.service.impl;

import com.sakollife.dto.response.MajorResultResponse;
import com.sakollife.dto.response.QuizSubmitResponse;
import com.sakollife.entity.*;
import com.sakollife.repository.*;
import com.sakollife.util.CosineSimilarityCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final MajorRepository majorRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final ProfileRepository profileRepository;
    private final VectorService vectorService;
    private final CosineSimilarityCalculator cosineSimilarityCalculator;

    @Transactional
    public QuizSubmitResponse submitQuiz(Map<String, String> answers, UUID userId) {

        double[] studentVector = vectorService.calculate(answers);

        List<Major> allMajors = majorRepository.findAll();
        if (allMajors.size() != 9) {
            log.warn("Expected 9 majors in DB, found {}. Please run the seed script.", allMajors.size());
        }

        List<MajorScore> scores = allMajors.stream()
                .map(major -> {
                    double[] majorVector = extractMajorVector(major);
                    double similarity = cosineSimilarityCalculator.calculate(studentVector, majorVector);
                    return new MajorScore(major, similarity);
                })
                .sorted(Comparator.comparingDouble(MajorScore::score).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < scores.size(); i++) {
            scores.get(i).rank = i + 1;
        }

        QuizAttempt attempt = null;
        int attemptNumber = 0;

        if (userId != null) {
            Profile user = profileRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("Profile not found for user: " + userId));

            quizAnswerRepository.deleteAllByUserId(userId);
            attemptNumber = quizAttemptRepository.findMaxAttemptNumberByUserId(userId) + 1;

            attempt = quizAttemptRepository.save(QuizAttempt.builder()
                    .user(user)
                    .attemptNumber(attemptNumber)
                    .vecR(BigDecimal.valueOf(studentVector[0]))
                    .vecI(BigDecimal.valueOf(studentVector[1]))
                    .vecA(BigDecimal.valueOf(studentVector[2]))
                    .vecS(BigDecimal.valueOf(studentVector[3]))
                    .vecE(BigDecimal.valueOf(studentVector[4]))
                    .vecC(BigDecimal.valueOf(studentVector[5]))
                    .build());

            final QuizAttempt savedAttempt = attempt;

            List<QuizResult> results = scores.stream()
                    .map(ms -> QuizResult.builder()
                            .attempt(savedAttempt)
                            .major(ms.major)
                            .similarityScore(BigDecimal.valueOf(ms.score))
                            .rank(ms.rank)
                            .build())
                    .collect(Collectors.toList());
            quizResultRepository.saveAll(results);

            saveAnswers(savedAttempt, answers);
            log.info("Quiz attempt #{} saved for user {}", attemptNumber, userId);
        }

        final UUID attemptId = attempt != null ? attempt.getId() : UUID.randomUUID();

        List<MajorResultResponse> filteredResults = scores.stream()
                .filter(ms -> ms.score >= 0.5)
                .map(ms -> MajorResultResponse.builder()
                        .majorId(ms.major.getId())
                        .code(ms.major.getCode())
                        .nameEn(ms.major.getNameEn())
                        .nameKh(ms.major.getNameKh())
                        .descriptionEn(ms.major.getDescriptionEn())
                        .descriptionKh(ms.major.getDescriptionKh())
                        .rank(ms.rank)
                        .similarityScore(ms.score)
                        .similarityPercentage((int) Math.round(ms.score * 100))
                        .build())
                .collect(Collectors.toList());

        return QuizSubmitResponse.builder()
                .attemptId(attemptId)
                .attemptNumber(attemptNumber)
                .studentVector(studentVector)
                .results(filteredResults)
                .build();
    }

    @Transactional
    public QuizSubmitResponse mergeGuestAttempt(Map<String, String> answers, UUID newUserId) {
        log.info("Merging guest attempt for newly registered user {}", newUserId);
        return submitQuiz(answers, newUserId);
    }

    public long getAttemptCount(UUID userId) {
        return quizAttemptRepository.countByUserId(userId);
    }

    private double[] extractMajorVector(Major major) {
        return new double[]{
                major.getRiasecR().doubleValue(),
                major.getRiasecI().doubleValue(),
                major.getRiasecA().doubleValue(),
                major.getRiasecS().doubleValue(),
                major.getRiasecE().doubleValue(),
                major.getRiasecC().doubleValue()
        };
    }

    /**
     * Re-computes ranked results for an existing attempt without writing to DB.
     * Used by GET /api/v1/quiz/latest-results.
     */
    public QuizSubmitResponse recomputeResults(Map<String, String> answers, QuizAttempt attempt) {
        double[] studentVector = vectorService.calculate(answers);

        List<Major> allMajors = majorRepository.findAll();
        List<MajorScore> scores = allMajors.stream()
                .map(major -> {
                    double[] majorVector = extractMajorVector(major);
                    double similarity = cosineSimilarityCalculator.calculate(studentVector, majorVector);
                    return new MajorScore(major, similarity);
                })
                .sorted(Comparator.comparingDouble(MajorScore::score).reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < scores.size(); i++) scores.get(i).rank = i + 1;

        List<MajorResultResponse> filteredResults = scores.stream()
                .filter(ms -> ms.score >= 0.5)
                .map(ms -> MajorResultResponse.builder()
                        .majorId(ms.major.getId())
                        .code(ms.major.getCode())
                        .nameEn(ms.major.getNameEn())
                        .nameKh(ms.major.getNameKh())
                        .descriptionEn(ms.major.getDescriptionEn())
                        .descriptionKh(ms.major.getDescriptionKh())
                        .rank(ms.rank)
                        .similarityScore(ms.score)
                        .similarityPercentage((int) Math.round(ms.score * 100))
                        .build())
                .collect(Collectors.toList());

        return QuizSubmitResponse.builder()
                .attemptId(attempt.getId())
                .attemptNumber(attempt.getAttemptNumber())
                .studentVector(studentVector)
                .results(filteredResults)
                .build();
    }

    private void saveAnswers(QuizAttempt attempt, Map<String, String> answers) {
        List<QuizAnswer> answerEntities = answers.entrySet().stream()
                .map(e -> QuizAnswer.builder()
                        .attempt(attempt)
                        .questionCode(e.getKey())
                        .answerValue(e.getValue())
                        .build())
                .collect(Collectors.toList());
        quizAnswerRepository.saveAll(answerEntities);
    }

    private static class MajorScore {
        final Major major;
        final double score;
        int rank;

        MajorScore(Major major, double score) {
            this.major = major;
            this.score = score;
        }

        double score() { return score; }
    }
}
