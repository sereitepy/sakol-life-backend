package com.sakollife.repository;
import com.sakollife.entity.UniversityMajorCareerProspect;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface UniversityMajorCareerProspectRepository extends JpaRepository<UniversityMajorCareerProspect, UUID> {
    List<UniversityMajorCareerProspect> findByUniversityMajorIdOrderByDisplayOrderAsc(UUID universityMajorId);
    void deleteByUniversityMajorId(UUID universityMajorId);
}