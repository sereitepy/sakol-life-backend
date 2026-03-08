package com.sakollife.repository;

import com.sakollife.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UniversityRepository extends JpaRepository<University, UUID> {
}
