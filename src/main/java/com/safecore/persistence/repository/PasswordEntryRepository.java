package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntryEntity, UUID> {

    /**
     * Recupera tutte le entry appartenenti a uno specifico utente.
     * Fondamentale per la Dashboard del Vault.
     */
    List<PasswordEntryEntity> findByUser(UserEntity user);

    /**
     * Alternativa: recupera tramite email dell'utente (comodo se usiamo SessionContext)
     */
    List<PasswordEntryEntity> findByUserEmail(String email);

    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}