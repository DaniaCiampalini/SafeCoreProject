package com.safecore.business.domain;
import java.util.UUID;

/**
 * Domain Model User.
 * Scelte di ingegneria del software:
 * - Classe IMMUTABILE → stato consistente
 * - Nessuna annotazione JPA
 * - Nessuna dipendenza da DB o framework
 * Questo oggetto rappresenta il concetto di "Utente"
 * nel dominio applicativo, NON nel database.
 */
public final class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final boolean mfaEnabled;

    // Costruttore package-private → controllato dal Builder
    User(UUID id, String email, String passwordHash, boolean mfaEnabled) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.mfaEnabled = mfaEnabled;
    }

    // SOLO getter → immutabilità
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }
}
