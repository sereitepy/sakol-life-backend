package com.sakollife.repository;

import com.sakollife.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface QuizResultRepository extends JpaRepository<QuizResult, UUID> {

    // Get all results for an attempt, filtered to >= 50% (0.5) and ranked
    @Query("""
        SELECT qr FROM QuizResult qr
        JOIN FETCH qr.major m
        WHERE qr.attempt.id = :attemptId
        AND qr.similarityScore >= 0.5
        ORDER BY qr.rank ASC
    """)
    List<QuizResult> findQualifyingResultsByAttemptId(@Param("attemptId") UUID attemptId);

    // Get ALL 9 results (for internal use, no filter)
    List<QuizResult> findByAttemptIdOrderByRankAsc(UUID attemptId);

    @Modifying
    @Transactional
    @Query("DELETE FROM QuizResult qr WHERE qr.major.id = :majorId")
    void deleteByMajorId(@Param("majorId") UUID majorId);

}
