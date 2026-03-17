package com.sakollife.repository;
import com.sakollife.entity.MajorSkill;
import com.sakollife.entity.enums.SkillType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface MajorSkillRepository extends JpaRepository<MajorSkill, UUID> {
    List<MajorSkill> findByMajorIdOrderByDisplayOrderAsc(UUID majorId);
    List<MajorSkill> findByMajorIdAndSkillTypeOrderByDisplayOrderAsc(UUID majorId, SkillType skillType);
    void deleteByMajorId(UUID majorId);
}