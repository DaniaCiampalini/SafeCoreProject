package com.safecore.business.domain;

/**
 * Domain Model User.
 *
 * Scelte di ingegneria del software:
 * - Classe IMMUTABILE → stato consistente
 * - Nessuna annotazione JPA
 * - Nessuna dipendenza da DB o framework
 *
 * Questo oggetto rappresenta il concetto di "Utente"
 * nel dominio applicativo, NON nel database.
 */
public final class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final boolean mfaEnabled;

    // Costruttore package-private → controllato dal Builder
    User(Long id, String email, String passwordHash, boolean mfaEnabled) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.mfaEnabled = mfaEnabled;
    }

    // SOLO getter → immutabilità
    public Long getId() {
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
