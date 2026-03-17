package com.sakollife.repository;
import com.sakollife.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface ScholarshipRepository extends JpaRepository<Scholarship, UUID> {
    List<Scholarship> findByUniversityId(UUID universityId);
    void deleteByUniversityId(UUID universityId);
}