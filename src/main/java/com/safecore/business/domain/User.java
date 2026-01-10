package com.safecore.business.domain;
import java.util.UUID;

/**
 * Questo è un Domain Model per l'Utente.
 * Nota le scelte tecniche:
 * - Classe IMMUTABILE (final e senza setter): una volta creato, l'oggetto non cambia.
 *   Questo evita un sacco di bug strani dovuti a stati inconsistenti.
 * - È un oggetto "puro": non sa nulla di database, JPA o Spring.
 *   Rappresenta l'utente nel cervello della nostra app, non sul disco.
 */
public final class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final boolean mfaEnabled;

    // Usiamo un costruttore package-private. 
    // L'idea è che solo il Builder (UserBuilder) possa creare istanze di User.
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
