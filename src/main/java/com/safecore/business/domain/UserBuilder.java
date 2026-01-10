package com.safecore.business.domain;

import java.util.UUID;

/**
 * Questo è il Builder per l'oggetto User.
 * Ci permette di "comporre" un utente un pezzo alla volta.
 * È molto più pulito che avere un costruttore gigantesco dove rischi di invertire
 * l'ordine delle stringhe (tipo scambiare email con l'hash della password).
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
     * Il momento della verità: crea l'oggetto User finale.
     */
    public User build() {
        if (email == null || passwordHash == null) {
            throw new IllegalStateException("Alt! Senza email e password non posso creare un utente.");
        }
        return new User(id, email, passwordHash, mfaEnabled);
    }
}
