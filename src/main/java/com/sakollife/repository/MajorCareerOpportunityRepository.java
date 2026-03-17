package com.sakollife.repository;
import com.sakollife.entity.MajorCareerOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface MajorCareerOpportunityRepository extends JpaRepository<MajorCareerOpportunity, UUID> {
    List<MajorCareerOpportunity> findByMajorIdOrderByDisplayOrderAsc(UUID majorId);
    void deleteByMajorId(UUID majorId);
}