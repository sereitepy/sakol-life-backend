package com.sakollife.controller;

import com.sakollife.dto.response.QuizSubmitResponse;
import com.sakollife.repository.QuestionRepository;
import com.sakollife.repository.QuizAttemptRepository;
import com.sakollife.service.impl.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/v1/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuizAttemptRepository quizAttemptRepository;
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
}
