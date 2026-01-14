package com.safecore.business.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Questa classe rappresenta una "voce" nel tuo vault delle password.
 * È un oggetto di dominio puro: niente JPA, niente Spring, solo logica.
 * È IMMUTABILE: una volta creato, non puoi più cambiare i suoi campi.
 * Se vuoi "modificare" una password, ne crei una nuova istanza.
 * Questo approccio evita un sacco di bug dovuti a stati dell'oggetto che cambiano sotto il naso.
 */
public final class PasswordEntry {
    private final UUID id;
    private final String serviceName;
    private final String username;
    private final byte[] encryptedPassword; // I byte illeggibili
    private final LocalDateTime createdAt;

    private PasswordEntry(Builder builder) {
        this.id = builder.id;
        this.serviceName = builder.serviceName;
        this.username = builder.username;
        // Facciamo una copia dei byte per essere sicuri che nessuno li modifichi da fuori
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
     * Usiamo il Builder Pattern perché costruire un oggetto con tanti parametri
     * è noioso e facile sbagliare l'ordine. Con il Builder è tutto più chiaro.
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
         * Crea l'oggetto finale. Qui controlliamo che i dati fondamentali ci siano tutti.
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