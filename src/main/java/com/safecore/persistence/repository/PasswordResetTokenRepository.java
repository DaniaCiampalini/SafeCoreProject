package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    // Questo nome deve corrispondere ESATTAMENTE a quello usato nel Service
    // Risolve l'errore "Cannot resolve method findByEmailAndUsedFalse"
    Optional<PasswordResetTokenEntity> findByEmailAndUsedFalse(String email);
}