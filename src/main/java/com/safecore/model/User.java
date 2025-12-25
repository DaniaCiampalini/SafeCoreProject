package com.safecore.model;
import java.util.Objects;
import java.util.UUID;

/* Entità domain che rappresenta un utente del sistema -> deve essere immutabile */

public final class User {
    private final UUID id;    // invece di int o long, UUID evita attacchi di enumerazione per prevedere l'ID*/
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean mfaEnabled;

    private User(Builder builder) {   // scelta Builder: costruttore complesso (#parametri>4)
        this.id = builder.id;         // Builder = inner class + costruttore privato, nessuno può creare User esterno
        this.username = builder.username;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.mfaEnabled = builder.mfaEnabled;
    }

    public UUID getId() {
        return id;
    }
    public String getUsername() {
        return username;
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
    // no setter, voglio stato consistente

    public static class Builder {

        private UUID id;
        private String username;
        private String email;
        private String passwordHash;
        private boolean mfaEnabled = false;

        public Builder id(UUID id) {
            this.id = id;
            return this;       // Fluent API -> concatenare chiamate con this
        }
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        public Builder passwordHash(String passwordHash) {
            this.passwordHash = passwordHash;
            return this;
        }
        public Builder mfaEnabled(boolean mfaEnabled) {
            this.mfaEnabled = mfaEnabled;
            return this;
        }

        public User build(){
            Objects.requireNonNull(id, "User id cannot be null");
            Objects.requireNonNull(username, "Username cannot be null");
            Objects.requireNonNull(email, "Email cannot be null");
            Objects.requireNonNull(passwordHash, "Password hash cannot be null");

            return new User(this);
        }
        /* Validazione e Contratti (guarda cap. 16) ->
        fail-first e gestisce casi di creazioni di utenti non validi */

    }

}



