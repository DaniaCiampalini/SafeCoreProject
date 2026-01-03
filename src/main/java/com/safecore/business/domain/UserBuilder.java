package com.safecore.business.domain;

import java.util.UUID;

/**
 * Builder Pattern per User.
 * Motivazione:
 * - Evita costruttori con molti parametri
 * - Rende esplicite le scelte di configurazione
 * - Facilita test unitari
 */
public class UserBuilder {

    private UUID id;
    private String email;
    private String passwordHash;
    private boolean mfaEnabled;

    public UserBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder passwordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public UserBuilder mfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
        return this;
    }

    /**
     * Costruisce un User valido.
     * Qui potresti aggiungere controlli di business.
     */
    public User build() {
        if (email == null || passwordHash == null) {
            throw new IllegalStateException("Email e password sono obbligatorie");
        }
        return new User(id, email, passwordHash, mfaEnabled);
    }
}
