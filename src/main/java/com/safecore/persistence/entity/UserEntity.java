package com.safecore.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Questa invece è l'Entity per il database.
 * A differenza del Domain Model, qui usiamo Hibernate/JPA per mappare
 * i campi direttamente sulla tabella "users".
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
    private String passwordHash; // Ovviamente salviamo l'hash, mai la password vera!

    @Column(nullable = false)
    private boolean mfaEnabled;

    public UserEntity() { } // JPA vuole sempre un costruttore vuoto

    // Costruttore di utilità per il Service
    public UserEntity(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.mfaEnabled = false;
    }

    // Getter e Setter
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String ph) { this.passwordHash = ph; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfa) { this.mfaEnabled = mfa; }
}