package com.safecore.model;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/* Entità domain che rappresenta una credenziale salvata -> deve essere immutabile */
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
    this.encryptedPassword = builder.encryptedPassword.clone();  //copia difensiva
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
        return encryptedPassword.clone();  //copia difensiva perchè array sono modificabili
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // scelta Builder per stesse motivi di User
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

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder encryptedPassword(byte[] encryptedPassword) {
            this.encryptedPassword = encryptedPassword;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PasswordEntry build() {
            Objects.requireNonNull(id, "ID is required");
            Objects.requireNonNull(serviceName, "Service Name is required");
            Objects.requireNonNull(username, "Username is required");
            Objects.requireNonNull(encryptedPassword, "Encrypted Password is required");
            Objects.requireNonNull(createdAt, "Creation Date is required");

            return new PasswordEntry(this);
        }
    }
}

