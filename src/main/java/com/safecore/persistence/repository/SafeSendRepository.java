package com.safecore.persistence.repository;

import com.safecore.persistence.entity.SafeSendEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SafeSendRepository extends JpaRepository<SafeSendEntry, UUID> {
    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}
