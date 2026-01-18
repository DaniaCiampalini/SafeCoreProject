package com.safecore.business.domain;

import java.util.UUID;

/**
 * Questo è il Builder per l'oggetto User.
 * Ci permette di costruire un utente un pezzo alla volta.
 * È molto più pulito che avere un costruttore con tanti parametri
 * (o peggio, tanti costruttori sovraccaricati).
 * Inoltre, possiamo aggiungere logica di validazione
 * prima di creare l'oggetto finale.
 */

public class UserBuilder {

    private UUID id;
    private String email;
    private String passwordHash;

    public UserBuilder id(UUID id) { // Scelta UUID per garantire unicità dell'ID
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


    /**
     * Crea l'oggetto User finale.
     */
    public User build() {
        if (email == null || passwordHash == null) {
            throw new IllegalStateException("Senza email e password non posso creare un utente!");
        }
        return new User(id, email, passwordHash);
    }
}
