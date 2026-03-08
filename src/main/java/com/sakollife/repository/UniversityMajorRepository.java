package com.sakollife.repository;

import com.sakollife.entity.UniversityMajor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface UniversityMajorRepository extends JpaRepository<UniversityMajor, UUID> {

    @Query("""
        SELECT um FROM UniversityMajor um
        JOIN FETCH um.university u
        WHERE um.major.id = :majorId
        AND (:type IS NULL OR u.type = :type)
        AND (:city IS NULL OR u.locationCity = :city)
        AND (:maxFee IS NULL OR um.tuitionFeeUsd <= :maxFee)
        AND (:durationYears IS NULL OR um.durationYears = :durationYears)
        ORDER BY um.tuitionFeeUsd ASC
    """)
    List<UniversityMajor> findByMajorWithFilters(
            @Param("majorId") UUID majorId,
            @Param("type") com.sakollife.entity.enums.UniversityType type,
            @Param("city") String city,
            @Param("maxFee") BigDecimal maxFee,
            @Param("durationYears") Integer durationYears);

    @Modifying
    @Transactional
    @Query("DELETE FROM UniversityMajor um WHERE um.university.id = :universityId")
    void deleteByUniversityId(@Param("universityId") UUID universityId);

    @Modifying
    @Transactional
    @Query("DELETE FROM UniversityMajor um WHERE um.major.id = :majorId")
    void deleteByMajorId(@Param("majorId") UUID majorId);
}