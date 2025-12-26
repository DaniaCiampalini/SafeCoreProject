package com.safecore.persistence.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity JPA per il reset della password.
 *
 * SE:
 * - Token hashato (mai salvare token in chiaro)
 * - Expiration + flag used
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    protected PasswordResetTokenEntity() {
        // JPA
    }

    public PasswordResetTokenEntity(String email, String tokenHash, LocalDateTime expiresAt) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void markUsed() {
        this.used = true;
    }
}
