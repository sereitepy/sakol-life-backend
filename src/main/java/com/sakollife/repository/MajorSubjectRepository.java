package com.sakollife.repository;
import com.sakollife.entity.MajorSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface MajorSubjectRepository extends JpaRepository<MajorSubject, UUID> {
    List<MajorSubject> findByMajorIdOrderByDisplayOrderAsc(UUID majorId);
    void deleteByMajorId(UUID majorId);
}