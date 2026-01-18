package com.safecore.persistence.repository;

import com.safecore.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository per gestire gli UserEntity nel database.
 * Estende JpaRepository per fornire operazioni CRUD di base.
 * Spring si occupa di scrivere SQL dietro le quinte.
 */

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Aggiorna la password di un utente.
     * Qui usiamo una query JPQL perché è un aggiornamento diretto.
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :pwd WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("pwd") String hashedPassword);
}