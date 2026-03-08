package com.sakollife.repository;

import com.sakollife.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {}
