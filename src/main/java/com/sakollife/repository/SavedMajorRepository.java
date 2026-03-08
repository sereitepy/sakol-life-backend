package com.sakollife.repository;

import com.sakollife.entity.SavedMajor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedMajorRepository extends JpaRepository<SavedMajor, UUID> {
    List<SavedMajor> findByUserIdOrderBySavedAtDesc(UUID userId);
    Optional<SavedMajor> findByUserIdAndMajorId(UUID userId, UUID majorId);
    void deleteByUserIdAndMajorId(UUID userId, UUID majorId);
    boolean existsByUserIdAndMajorId(UUID userId, UUID majorId);
}
