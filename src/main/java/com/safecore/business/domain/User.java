package com.safecore.business.domain;

import java.util.UUID;

/**
 * Questo è un Domain Model per l'Utente.
 * - Classe IMMUTABILE (final e senza setter): una volta creato, l'oggetto non cambia.
 * Questo evita un sacco di bug strani dovuti a stati inconsistenti.
 * - È un oggetto "puro": non sa nulla di database, JPA o Spring.
 * Rappresenta l'utente nel cervello della nostra app, non sul disco.
 * - Ha un costruttore package-private: solo il Builder (UserBuilder) può creare istanze.
 * - Ha solo getter: nessun modo di cambiare lo stato dopo la creazione.
 */

public final class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;

    // Costruttore package-private: solo il Builder può usarlo
    User(UUID id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }


    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
