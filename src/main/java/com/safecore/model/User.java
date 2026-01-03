package com.safecore.model;
import java.util.Objects;
import java.util.UUID;

/**
 * Rappresenta l'utente nel nostro "mondo" (Domain).
 * Ho usato UUID invece di ID numerici (1, 2, 3...) così un malintenzionato
 * non può "indovinare" quanti utenti abbiamo o provare ad accedere a ID sequenziali.
 */
public final class User {
    private final UUID id;
    private final String username;
    private final String email;
    private final String passwordHash;
    private final boolean mfaEnabled;

    private User(Builder builder) {
        this.id = builder.id;
        this.username = builder.username;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.mfaEnabled = builder.mfaEnabled;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isMfaEnabled() { return mfaEnabled; }

    // Risoluzione Warning: toString() utile per il logging in fase di sviluppo
    @Override
    public String toString() {
        return "User{email='" + email + "', mfa=" + mfaEnabled + "}";
    }

    public static class Builder {
        private UUID id;
        private String username;
        private String email;
        private String passwordHash;
        private boolean mfaEnabled = false;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder username(String u) { this.username = u; return this; }
        public Builder email(String e) { this.email = e; return this; }
        public Builder passwordHash(String p) { this.passwordHash = p; return this; }
        public Builder mfaEnabled(boolean m) { this.mfaEnabled = m; return this; }

        public com.safecore.model.User build(){
            Objects.requireNonNull(id);
            Objects.requireNonNull(email);
            return new User(this);
        }
    }
}