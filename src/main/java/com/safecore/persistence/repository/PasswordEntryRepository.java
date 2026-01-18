package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository per gestire le PasswordEntry nel database.
 * Estende JpaRepository per fornire operazioni CRUD di base.
 * Spring si occupa di scrivere SQl dietro le quinte.
 */

@Repository
public interface PasswordEntryRepository extends JpaRepository<PasswordEntryEntity, UUID> {

    //Trova tutte le password salvate da un certo utente.
    List<PasswordEntryEntity> findByUser(UserEntity user);

    List<PasswordEntryEntity> findByUserEmail(String email);

    void deleteByExpiresAtBefore(java.time.LocalDateTime now);
}