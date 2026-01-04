package com.safecore.persistence.entity;

import javax.persistence.*;
import java.util.UUID;

/**
 * Questa è l'immagine riflessa della tabella 'users' sul database.
 * A differenza del nostro 'User' di business, questa deve essere mutabile (ha i setter)
 * perché Hibernate deve poterci scrivere dentro quando legge dal database.
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean mfaEnabled;

    public UserEntity() { } // Obbligatorio per JPA

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String ph) { this.passwordHash = ph; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(boolean mfa) { this.mfaEnabled = mfa; }
}