package com.sakollife.repository;
import com.sakollife.entity.AdmissionRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface AdmissionRequirementRepository extends JpaRepository<AdmissionRequirement, UUID> {
    List<AdmissionRequirement> findByUniversityIdOrderByDisplayOrderAsc(UUID universityId);
    void deleteByUniversityId(UUID universityId);
}