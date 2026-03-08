package com.sakollife.controller.admin;

import com.sakollife.entity.AnswerOption;
import com.sakollife.entity.Question;
import com.sakollife.entity.enums.QuestionFormat;
import com.sakollife.repository.AnswerOptionRepository;
import com.sakollife.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.LinkedHashMap;
import java.util.List;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    /**
     * GET /api/v1/admin/questions
     * Returns all questions WITHOUT options (clean list for admin table view).
     * To see options for a specific question, use GET /admin/questions/{id}/options
     */
    @GetMapping
    public ResponseEntity<?> getAllQuestions() {
        List<Question> questions = questionRepository.findAll(
                org.springframework.data.domain.Sort.by("displayOrder")
        );
        // Map to a clean DTO — no options, no circular refs
        List<Map<String, Object>> result = questions.stream().map(q -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",            q.getId());
            map.put("questionCode",  q.getQuestionCode());
            map.put("textEn",        q.getTextEn());
            map.put("textKh",        q.getTextKh());
            map.put("format",        q.getFormat());
            map.put("weight",        q.getWeight());
            map.put("displayOrder",  q.getDisplayOrder());
            map.put("active",        q.getActive());
            map.put("likertR",       q.getLikertR());
            map.put("likertI",       q.getLikertI());
            map.put("likertA",       q.getLikertA());
            map.put("likertS",       q.getLikertS());
            map.put("likertE",       q.getLikertE());
            map.put("likertC",       q.getLikertC());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/admin/questions/{id}
     * Returns a single question WITH its full options list.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getQuestion(@PathVariable UUID id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found"));

        List<AnswerOption> options = answerOptionRepository
                .findByQuestionIdOrderByDisplayOrderAsc(id);

        // Build options without the back-reference to question
        List<Map<String, Object>> optionMaps = options.stream().map(o -> {
            Map<String, Object> om = new LinkedHashMap<>();
            om.put("id",           o.getId());
            om.put("optionLetter", o.getOptionLetter());
            om.put("textEn",       o.getTextEn());
            om.put("textKh",       o.getTextKh());
            om.put("scoreValue",   o.getScoreValue());
            om.put("displayOrder", o.getDisplayOrder());
            om.put("riasecR",      o.getRiasecR());
            om.put("riasecI",      o.getRiasecI());
            om.put("riasecA",      o.getRiasecA());
            om.put("riasecS",      o.getRiasecS());
            om.put("riasecE",      o.getRiasecE());
            om.put("riasecC",      o.getRiasecC());
            return om;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",           q.getId());
        result.put("questionCode", q.getQuestionCode());
        result.put("textEn",       q.getTextEn());
        result.put("textKh",       q.getTextKh());
        result.put("format",       q.getFormat());
        result.put("weight",       q.getWeight());
        result.put("displayOrder", q.getDisplayOrder());
        result.put("active",       q.getActive());
        result.put("likertR",      q.getLikertR());
        result.put("likertI",      q.getLikertI());
        result.put("likertA",      q.getLikertA());
        result.put("likertS",      q.getLikertS());
        result.put("likertE",      q.getLikertE());
        result.put("likertC",      q.getLikertC());
        result.put("options",      optionMaps);

        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/admin/questions
     * Create a new question.
     * Body example:
     * {
     *   "questionCode": "Q15",
     *   "textEn": "How interested are you in cloud computing?",
     *   "textKh": "...",
     *   "format": "LIKERT",
     *   "weight": 1.5,
     *   "displayOrder": 15,
     *   "active": true,
     *   "likertR": 0.0, "likertI": 1.0, "likertA": 0.0,
     *   "likertS": 0.0, "likertE": 0.0, "likertC": 1.0
     * }
     */
    @PostMapping
    public ResponseEntity<?> createQuestion(@RequestBody Map<String, Object> body) {
        String code = (String) body.get("questionCode");
        if (questionRepository.existsByQuestionCode(code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question code already exists: " + code));
        }

        Question question = Question.builder()
                .questionCode(code)
                .textEn((String) body.get("textEn"))
                .textKh((String) body.get("textKh"))
                .format(QuestionFormat.valueOf((String) body.get("format")))
                .weight(new BigDecimal(body.get("weight").toString()))
                .displayOrder((Integer) body.get("displayOrder"))
                .active(body.getOrDefault("active", true).equals(true))
                .likertR(bd(body, "likertR"))
                .likertI(bd(body, "likertI"))
                .likertA(bd(body, "likertA"))
                .likertS(bd(body, "likertS"))
                .likertE(bd(body, "likertE"))
                .likertC(bd(body, "likertC"))
                .build();

        return ResponseEntity.ok(questionRepository.save(question));
    }

    /**
     * PUT /api/v1/admin/questions/{id}
     * Update question text, weight, format, RIASEC mappings, active status.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuestion(@PathVariable UUID id,
                                             @RequestBody Map<String, Object> body) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found"));

        if (body.containsKey("textEn"))      question.setTextEn((String) body.get("textEn"));
        if (body.containsKey("textKh"))      question.setTextKh((String) body.get("textKh"));
        if (body.containsKey("weight"))      question.setWeight(new BigDecimal(body.get("weight").toString()));
        if (body.containsKey("displayOrder")) question.setDisplayOrder((Integer) body.get("displayOrder"));
        if (body.containsKey("active"))      question.setActive((Boolean) body.get("active"));
        if (body.containsKey("format"))      question.setFormat(QuestionFormat.valueOf((String) body.get("format")));
        if (body.containsKey("likertR"))     question.setLikertR(bd(body, "likertR"));
        if (body.containsKey("likertI"))     question.setLikertI(bd(body, "likertI"));
        if (body.containsKey("likertA"))     question.setLikertA(bd(body, "likertA"));
        if (body.containsKey("likertS"))     question.setLikertS(bd(body, "likertS"));
        if (body.containsKey("likertE"))     question.setLikertE(bd(body, "likertE"));
        if (body.containsKey("likertC"))     question.setLikertC(bd(body, "likertC"));

        return ResponseEntity.ok(questionRepository.save(question));
    }

    /**
     * DELETE /api/v1/admin/questions/{id}
     * Soft delete — sets active = false (preserves historical quiz data).
     * Use hard delete only if you're sure no quiz attempts reference this question.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateQuestion(@PathVariable UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found"));
        question.setActive(false);
        questionRepository.save(question);
        return ResponseEntity.ok(Map.of("message", "Question deactivated", "id", id));
    }


    /**
     * GET /api/v1/admin/questions/{id}/options
     */
    @GetMapping("/{id}/options")
    public ResponseEntity<?> getOptions(@PathVariable UUID id) {
        return ResponseEntity.ok(answerOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(id));
    }

    /**
     * POST /api/v1/admin/questions/{id}/options
     * Add an answer option to a question.
     * Body example:
     * {
     *   "optionLetter": "E",
     *   "textEn": "Managing systems and processes",
     *   "textKh": "...",
     *   "scoreValue": 4,
     *   "displayOrder": 5,
     *   "riasecR": 0.0, "riasecI": 0.0, "riasecA": 0.0,
     *   "riasecS": 0.0, "riasecE": 1.0, "riasecC": 1.0
     * }
     */
    @PostMapping("/{id}/options")
    public ResponseEntity<?> addOption(@PathVariable UUID id,
                                        @RequestBody Map<String, Object> body) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Question not found"));

        AnswerOption option = AnswerOption.builder()
                .question(question)
                .optionLetter((String) body.get("optionLetter"))
                .textEn((String) body.get("textEn"))
                .textKh((String) body.get("textKh"))
                .scoreValue(body.containsKey("scoreValue") ? (Integer) body.get("scoreValue") : 4)
                .displayOrder(body.containsKey("displayOrder") ? (Integer) body.get("displayOrder") : 0)
                .riasecR(bd(body, "riasecR"))
                .riasecI(bd(body, "riasecI"))
                .riasecA(bd(body, "riasecA"))
                .riasecS(bd(body, "riasecS"))
                .riasecE(bd(body, "riasecE"))
                .riasecC(bd(body, "riasecC"))
                .build();

        return ResponseEntity.ok(answerOptionRepository.save(option));
    }

    /**
     * PUT /api/v1/admin/questions/{questionId}/options/{optionId}
     * Update an answer option's text, score, or RIASEC contributions.
     */
    @PutMapping("/{questionId}/options/{optionId}")
    public ResponseEntity<?> updateOption(@PathVariable UUID questionId,
                                           @PathVariable UUID optionId,
                                           @RequestBody Map<String, Object> body) {
        AnswerOption option = answerOptionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("Answer option not found"));

        if (body.containsKey("textEn"))       option.setTextEn((String) body.get("textEn"));
        if (body.containsKey("textKh"))       option.setTextKh((String) body.get("textKh"));
        if (body.containsKey("scoreValue"))   option.setScoreValue((Integer) body.get("scoreValue"));
        if (body.containsKey("displayOrder")) option.setDisplayOrder((Integer) body.get("displayOrder"));
        if (body.containsKey("riasecR"))      option.setRiasecR(bd(body, "riasecR"));
        if (body.containsKey("riasecI"))      option.setRiasecI(bd(body, "riasecI"));
        if (body.containsKey("riasecA"))      option.setRiasecA(bd(body, "riasecA"));
        if (body.containsKey("riasecS"))      option.setRiasecS(bd(body, "riasecS"));
        if (body.containsKey("riasecE"))      option.setRiasecE(bd(body, "riasecE"));
        if (body.containsKey("riasecC"))      option.setRiasecC(bd(body, "riasecC"));

        return ResponseEntity.ok(answerOptionRepository.save(option));
    }

    /**
     * DELETE /api/v1/admin/questions/{questionId}/options/{optionId}
     */
    @DeleteMapping("/{questionId}/options/{optionId}")
    public ResponseEntity<?> deleteOption(@PathVariable UUID questionId,
                                           @PathVariable UUID optionId) {
        answerOptionRepository.deleteById(optionId);
        return ResponseEntity.ok(Map.of("message", "Option deleted", "id", optionId));
    }

    private BigDecimal bd(Map<String, Object> body, String key) {
        Object val = body.get(key);
        if (val == null) return BigDecimal.ZERO;
        return new BigDecimal(val.toString());
    }
}
