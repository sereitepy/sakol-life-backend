package com.sakollife.controller;

import com.sakollife.dto.response.QuizSubmitResponse;
import com.sakollife.entity.QuizAnswer;
import com.sakollife.entity.QuizAttempt;
import com.sakollife.repository.QuestionRepository;
import com.sakollife.repository.QuizAnswerRepository;
import com.sakollife.repository.QuizAttemptRepository;
import com.sakollife.service.impl.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuestionRepository questionRepository;

    /**
     * GET /api/v1/quiz/questions
     */
    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions() {
        List<Map<String, Object>> questions = questionRepository.findAllActiveWithOptions()
                .stream()
                .map(q -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", q.getId());
                    map.put("questionCode", q.getQuestionCode());
                    map.put("textEn", q.getTextEn());
                    map.put("textKh", q.getTextKh());
                    map.put("likertLabelLowEn", q.getLikertLabelLowEn());
                    map.put("likertLabelHighEn", q.getLikertLabelHighEn());
                    map.put("likertLabelLowKh", q.getLikertLabelLowKh());
                    map.put("likertLabelHighKh", q.getLikertLabelHighKh());
                    map.put("format", q.getFormat());
                    map.put("displayOrder", q.getDisplayOrder());
                    map.put("options", q.getOptions().stream().map(o -> {
                        Map<String, Object> opt = new HashMap<>();
                        opt.put("optionLetter", o.getOptionLetter());
                        opt.put("textEn", o.getTextEn());
                        opt.put("textKh", o.getTextKh());
                        opt.put("displayOrder", o.getDisplayOrder());
                        return opt;
                    }).toList());
                    return map;
                }).toList();

        return ResponseEntity.ok(questions);
    }

    /**
     * POST /api/v1/quiz/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResponse> submitQuiz(
            @RequestBody Map<String, String> answers,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(quizService.submitQuiz(answers, userId));
    }

    /**
     * POST /api/v1/quiz/merge-guest-attempt
     * Called right after registration to save the guest's quiz as attempt #1.
     * Body: same flat map format as /submit
     */
    @PostMapping("/merge-guest-attempt")
    public ResponseEntity<QuizSubmitResponse> mergeGuestAttempt(
            @RequestBody Map<String, String> answers,
            Authentication authentication) {

        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(quizService.mergeGuestAttempt(answers, userId));
    }

    /**
     * GET /api/v1/quiz/history
     * Returns total quiz attempt count for the authenticated user.
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        long count = quizService.getAttemptCount(userId);
        return ResponseEntity.ok(Map.of("totalAttempts", count));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return (UUID) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * GET /api/v1/quiz/latest-results
     * Re-computes and returns ranked major results for the user's most recent attempt.
     * Used to restore the results page when sessionStorage has been cleared.
     */
    @GetMapping("/latest-results")
    public ResponseEntity<?> getLatestResults(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        Optional<QuizAttempt> latestAttempt = quizAttemptRepository.findLatestByUserId(userId);
        if (latestAttempt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No quiz attempt found"));
        }

        QuizAttempt attempt = latestAttempt.get();

        // Re-hydrate answers from saved quiz_answers rows
        List<QuizAnswer> savedAnswers = quizAnswerRepository
                .findByAttemptIdOrderByQuestionCodeAsc(attempt.getId());

        Map<String, String> answers = savedAnswers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        QuizAnswer::getQuestionCode,
                        QuizAnswer::getAnswerValue
                ));

        if (answers.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "No answers found for latest attempt"));
        }

        // Re-run the same scoring logic — no DB writes this time
        QuizSubmitResponse response = quizService.recomputeResults(answers, attempt);
        return ResponseEntity.ok(response);
    }
}
