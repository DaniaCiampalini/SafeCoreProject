package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository per gestire i token di reset della password nel database.
 * Estende JpaRepository per fornire operazioni CRUD di base.
 * Spring si occupa di scrivere SQL dietro le quinte.
 */

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {

    // Trova un token di reset della password non ancora usato per una certa email.
    Optional<PasswordResetTokenEntity> findByEmailAndUsedFalse(String email);
}