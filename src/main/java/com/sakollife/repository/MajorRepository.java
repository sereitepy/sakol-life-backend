package com.sakollife.repository;

import com.sakollife.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MajorRepository extends JpaRepository<Major, UUID> {
    Optional<Major> findByCode(String code);
}
