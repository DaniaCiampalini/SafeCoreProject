package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

/**
 * Il magazzino delle password.
 * Anche qui lasciamo che Spring faccia il lavoro sporco di creare le query SQL.
 */
@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntryEntity, UUID> {

    /**
     * Trova tutte le password salvate da un certo utente.
     */
    List<PasswordEntryEntity> findByUser(UserEntity user);

    /**
     * Comodo per cercare le password partendo dall'email dell'utente loggato.
     */
    List<PasswordEntryEntity> findByUserEmail(String email);

    /**
     * Elimina tutte le entry che hanno superato la loro data di scadenza.
     */
    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}