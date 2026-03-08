package com.sakollife.repository;

import com.sakollife.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnswerOptionRepository extends JpaRepository<AnswerOption, UUID> {

    List<AnswerOption> findByQuestionIdOrderByDisplayOrderAsc(UUID questionId);

    Optional<AnswerOption> findByQuestionIdAndOptionLetter(UUID questionId, String optionLetter);

    void deleteByQuestionId(UUID questionId);
}
