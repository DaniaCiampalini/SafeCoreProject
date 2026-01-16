package com.safecore.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_entries")
public class PasswordEntryEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, columnDefinition = "VARBINARY(1024)")
    private byte[] encryptedPassword;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore // Fondamentale: impedisce a Jackson di rompere la sessione Hibernate
    private UserEntity user;

    public PasswordEntryEntity() {
        this.createdAt = LocalDateTime.now();
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public byte[] getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(byte[] encryptedPassword) { this.encryptedPassword = encryptedPassword; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}