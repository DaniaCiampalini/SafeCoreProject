package com.safecore.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entity JPA che rappresenta un utente nel database.
 * Mappa la tabella "users".
 * Contiene campi per email, password hash e stato MFA.
 * Usata da UserRepository per operazioni CRUD sugli utenti.
 * Spring Data JPA si occupa di generare il SQL dietro le quinte.
 */

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue // Lasciamo che sia il DB a generare l'UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    public UserEntity() {
    } // JPA vuole sempre un costruttore vuoto

    // Costruttore di utilità per il Service
    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String ph) {
        this.passwordHash = ph;
    }

}