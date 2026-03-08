package com.sakollife.repository;

import com.sakollife.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    // Load all active questions with their answer options (for quiz display)
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options WHERE q.active = true ORDER BY q.displayOrder ASC")
    List<Question> findAllActiveWithOptions();

    // Load ALL questions (for admin panel)
    @Query("SELECT q FROM Question q LEFT JOIN FETCH q.options ORDER BY q.displayOrder ASC")
    List<Question> findAllWithOptions();

    Optional<Question> findByQuestionCode(String questionCode);

    boolean existsByQuestionCode(String questionCode);
}
