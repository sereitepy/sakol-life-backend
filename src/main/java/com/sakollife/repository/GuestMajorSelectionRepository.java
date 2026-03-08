package com.sakollife.repository;

import com.sakollife.entity.GuestMajorSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface GuestMajorSelectionRepository extends JpaRepository<GuestMajorSelection, UUID> {
    Optional<GuestMajorSelection> findByGuestSessionId(UUID guestSessionId);

    @Modifying
    @Transactional
    void deleteByGuestSessionId(UUID guestSessionId);
}
