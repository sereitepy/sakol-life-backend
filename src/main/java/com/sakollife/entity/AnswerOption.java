package com.sakollife.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "answer_options",
       uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "option_letter"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // "A", "B", "C", "D", "E", "F", "G", "H"
    @Column(name = "option_letter", nullable = false, length = 2)
    private String optionLetter;

    @Column(name = "text_en", nullable = false, columnDefinition = "TEXT")
    private String textEn;

    @Column(name = "text_kh", nullable = false, columnDefinition = "TEXT")
    private String textKh;

    // RIASEC dimension contributions (0.0 = no contribution, 1.0 = contributes)
    @Column(name = "riasec_r", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecR = BigDecimal.ZERO;
    @Column(name = "riasec_i", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecI = BigDecimal.ZERO;
    @Column(name = "riasec_a", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecA = BigDecimal.ZERO;
    @Column(name = "riasec_s", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecS = BigDecimal.ZERO;
    @Column(name = "riasec_e", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecE = BigDecimal.ZERO;
    @Column(name = "riasec_c", nullable = false, precision = 3, scale = 1) @Builder.Default private BigDecimal riasecC = BigDecimal.ZERO;

    // Score value this option carries (default 4: "good" answer on a 1-5 scale)
    // Admin can fine-tune: e.g. "A = very familiar" might be 5, "D = new to tech" = 1
    @Column(name = "score_value", nullable = false)
    @Builder.Default
    private Integer scoreValue = 4;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
