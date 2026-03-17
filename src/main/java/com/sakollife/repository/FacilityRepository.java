package com.sakollife.repository;
import com.sakollife.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface FacilityRepository extends JpaRepository<Facility, UUID> {
    List<Facility> findByUniversityIdOrderByDisplayOrderAsc(UUID universityId);
    void deleteByUniversityId(UUID universityId);
}