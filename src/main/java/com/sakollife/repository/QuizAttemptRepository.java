package com.sakollife.repository;

import com.sakollife.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

    List<QuizAttempt> findByUserIdOrderByAttemptNumberDesc(UUID userId);

    // Count total attempts for a user (shown in profile dashboard)
    long countByUserId(UUID userId);

    // Get the latest attempt for a user
    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user.id = :userId ORDER BY qa.attemptNumber DESC LIMIT 1")
    Optional<QuizAttempt> findLatestByUserId(@Param("userId") UUID userId);

    // Get the max attempt number for a user (to increment on new submission)
    @Query("SELECT COALESCE(MAX(qa.attemptNumber), 0) FROM QuizAttempt qa WHERE qa.user.id = :userId")
    int findMaxAttemptNumberByUserId(@Param("userId") UUID userId);
}
