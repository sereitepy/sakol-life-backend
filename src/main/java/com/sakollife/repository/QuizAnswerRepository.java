package com.sakollife.repository;

import com.sakollife.entity.QuizAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {

    List<QuizAnswer> findByAttemptIdOrderByQuestionCodeAsc(UUID attemptId);

    // Delete all answers for a specific user's previous attempt (before inserting new ones)
    @Modifying
    @Query("""
        DELETE FROM QuizAnswer qa
        WHERE qa.attempt.id IN (
            SELECT a.id FROM QuizAttempt a WHERE a.user.id = :userId
        )
    """)
    void deleteAllByUserId(@Param("userId") UUID userId);
}
