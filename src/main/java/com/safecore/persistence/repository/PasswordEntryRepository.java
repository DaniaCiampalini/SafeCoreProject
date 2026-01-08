package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

/**
 * Repository per la gestione delle password cifrate.
 * Estendendo JpaRepository otteniamo gratis:
 * - save(entity)
 * - findAll()
 * - deleteById(id)
 * - findById(id)
 */
@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntryEntity, UUID> {
    // Qui puoi aggiungere metodi personalizzati se servissero,
    // ad esempio: List<PasswordEntryEntity> findByServiceNameContaining(String name);
}