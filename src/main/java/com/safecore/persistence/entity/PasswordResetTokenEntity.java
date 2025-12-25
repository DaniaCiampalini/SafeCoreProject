package com.safecore.persistence.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity che rappresenta un token di reset password.
 * Il token è salvato in forma HASHATA per sicurezza.
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

    protected PasswordResetTokenEntity() {}

    public PasswordResetTokenEntity(String email, String tokenHash, LocalDateTime expiresAt) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public String getEmail() {
        return email;
    }

}
