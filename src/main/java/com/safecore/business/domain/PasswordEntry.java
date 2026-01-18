package com.safecore.business.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Questa classe rappresenta una voce di password nel vault.
 * È un oggetto di dominio puro: niente JPA, niente Spring, solo logica.
 * È IMMUTABILE. Se si vuole "modificare" una password, se ne crea una nuova istanza.
 * Usa il Builder Pattern per una costruzione più chiara.
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
        // Facciamo una copia dei byte per essere sicuri
        this.encryptedPassword = builder.encryptedPassword != null ? builder.encryptedPassword.clone() : null;
        this.createdAt = builder.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getEncryptedPassword() {
        return encryptedPassword != null ? encryptedPassword.clone() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordEntry that = (PasswordEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Usiamo il Builder Pattern perché ci sono molti campi.
     * Permette di costruire l'oggetto passo passo in modo leggibile.
     */
    public static class Builder {
        private UUID id;
        private String serviceName;
        private String username;
        private byte[] encryptedPassword;
        private LocalDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder serviceName(String sn) {
            this.serviceName = sn;
            return this;
        }

        public Builder username(String un) {
            this.username = un;
            return this;
        }

        public Builder encryptedPassword(byte[] ep) {
            this.encryptedPassword = ep;
            return this;
        }

        public Builder createdAt(LocalDateTime ca) {
            this.createdAt = ca;
            return this;
        }

        /**
         * Costruisce l'oggetto PasswordEntry finale.
         * Verifica che i campi obbligatori siano presenti.
         */
        public PasswordEntry build() {
            Objects.requireNonNull(serviceName, "Nome servizio obbligatorio");
            Objects.requireNonNull(username, "Username obbligatorio");
            Objects.requireNonNull(encryptedPassword, "Password cifrata obbligatoria");

            if (createdAt == null) this.createdAt = LocalDateTime.now();

            return new PasswordEntry(this);
        }
    }
}