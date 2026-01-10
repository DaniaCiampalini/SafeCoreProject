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
 * Questo è il magazzino degli utenti.
 * Grazie a Spring Data JPA, non dobbiamo scrivere il codice per salvare o cercare
 * gli utenti sul database: ci basta definire i metodi con i nomi giusti e 
 * Spring capisce da solo cosa vogliamo fare.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Cerca un utente tramite la sua email.
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Controlla se un'email è già registrata.
     */
    boolean existsByEmail(String email);

    /**
     * Aggiorna la password di un utente. 
     * Qui usiamo una query JPQL perché è un aggiornamento diretto.
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.passwordHash = :pwd WHERE u.email = :email")
    void updatePassword(@Param("email") String email, @Param("pwd") String hashedPassword);
}