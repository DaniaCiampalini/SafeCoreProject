package com.safecore.model;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Questa è l'entità che rappresenta una singola password salvata.
 * Nota: l'ho fatta 'final' e immutabile.
 * Se dobbiamo cambiare una password, creiamo un nuovo oggetto invece di modificare questo.
 * È molto più sicuro per evitare bug strani durante l'esecuzione.
 */
public final class PasswordEntry {
    private final UUID id;
    private final String serviceName;
    private final String username;
    private final byte[] encryptedPassword;
    private final LocalDateTime createdAt;

    private PasswordEntry(Builder builder) {
        this.id = builder.id;
        this.serviceName = builder.serviceName;
        this.username = builder.username;
        this.encryptedPassword = builder.encryptedPassword.clone(); // Copia difensiva: non vogliamo modifiche esterne
        this.createdAt = builder.createdAt;
    }

    // Getters utilizzati dalla UI per visualizzare la lista password
    public UUID getId() { return id; }
    public String getServiceName() { return serviceName; }
    public String getUsername() { return username; }
    public byte[] getEncryptedPassword() { return encryptedPassword.clone(); }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Risoluzione Warning: Implementati equals e hashCode per permettere confronti e uso in Set/Map
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordEntry that = (PasswordEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    // Builder Pattern: lo usiamo perché costruire l'oggetto con 5 parametri
    // nel costruttore sarebbe un incubo da leggere.
    public static class Builder {
        private UUID id;
        private String serviceName;
        private String username;
        private byte[] encryptedPassword;
        private LocalDateTime createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder serviceName(String sn) { this.serviceName = sn; return this; }
        public Builder username(String un) { this.username = un; return this; }
        public Builder encryptedPassword(byte[] ep) { this.encryptedPassword = ep; return this; }
        public Builder createdAt(LocalDateTime ca) { this.createdAt = ca; return this; }

        public PasswordEntry build() {
            // RIMOSSO: Objects.requireNonNull(id, "ID obbligatorio");
            // L'ID può essere null per le nuove entry
            Objects.requireNonNull(serviceName, "Nome servizio obbligatorio");
            Objects.requireNonNull(username, "Username obbligatorio");
            Objects.requireNonNull(encryptedPassword, "Password cifrata obbligatoria");

            if (createdAt == null) this.createdAt = LocalDateTime.now();

            return new PasswordEntry(this);
        }
    }
}