package com.sakollife.repository;
import com.sakollife.entity.TuitionFeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface TuitionFeeStructureRepository extends JpaRepository<TuitionFeeStructure, UUID> {
    List<TuitionFeeStructure> findByUniversityIdOrderByDisplayOrderAsc(UUID universityId);
    void deleteByUniversityId(UUID universityId);
}