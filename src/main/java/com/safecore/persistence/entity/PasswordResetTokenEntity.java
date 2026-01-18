package com.safecore.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity JPA che rappresenta un token di reset della password nel database.
 * Mappa la tabella "password_reset_tokens".
 * Contiene campi per email, hash del token, data di scadenza e stato di utilizzo.
 * Usata da PasswordResetTokenRepository per operazioni CRUD sui token di reset della password.
 * Spring Data JPA si occupa di generare il SQL dietro le quinte.
 */


@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String tokenHash;
    private LocalDateTime expiryDate;
    private boolean used;

    // Costruttore vuoto richiesto da JPA
    public PasswordResetTokenEntity() {
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

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}