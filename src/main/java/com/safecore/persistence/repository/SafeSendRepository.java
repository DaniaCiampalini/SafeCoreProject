package com.safecore.persistence.repository;

import com.safecore.persistence.entity.SafeSendEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SafeSendRepository extends JpaRepository<SafeSendEntryEntity, UUID> {

    @Modifying
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime now);
}