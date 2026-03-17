package com.sakollife.entity;

import com.sakollife.entity.enums.QuestionFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // e.g. "Q1", "Q2", "Q3", "Q4_A" ... "Q14"
    @Column(name = "question_code", nullable = false, unique = true, length = 10)
    private String questionCode;

    @Column(name = "text_en", nullable = false, columnDefinition = "TEXT")
    private String textEn;

    @Column(name = "text_kh", nullable = false, columnDefinition = "TEXT")
    private String textKh;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionFormat format;

    // How much this question's score is multiplied when building the vector
    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal weight;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // For LIKERT questions: which RIASEC dimensions does this score contribute to?
    // Set to 1.0 for dimensions it affects, 0.0 for dimensions it doesn't.
    // For SINGLE_CHOICE questions, these are ignored (contributions are on AnswerOption).
    @Column(name = "likert_r", precision = 3, scale = 1) @Builder.Default private BigDecimal likertR = BigDecimal.ZERO;
    @Column(name = "likert_i", precision = 3, scale = 1) @Builder.Default private BigDecimal likertI = BigDecimal.ZERO;
    @Column(name = "likert_a", precision = 3, scale = 1) @Builder.Default private BigDecimal likertA = BigDecimal.ZERO;
    @Column(name = "likert_s", precision = 3, scale = 1) @Builder.Default private BigDecimal likertS = BigDecimal.ZERO;
    @Column(name = "likert_e", precision = 3, scale = 1) @Builder.Default private BigDecimal likertE = BigDecimal.ZERO;
    @Column(name = "likert_c", precision = 3, scale = 1) @Builder.Default private BigDecimal likertC = BigDecimal.ZERO;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionLetter ASC")
    @Builder.Default
    private List<AnswerOption> options = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
