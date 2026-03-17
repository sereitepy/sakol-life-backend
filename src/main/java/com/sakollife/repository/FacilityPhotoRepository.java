package com.sakollife.repository;
import com.sakollife.entity.FacilityPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface FacilityPhotoRepository extends JpaRepository<FacilityPhoto, UUID> {
    List<FacilityPhoto> findByUniversityIdOrderByDisplayOrderAsc(UUID universityId);
    void deleteByUniversityId(UUID universityId);
}