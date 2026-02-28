package com.safecore.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Rappresenta una voce di Safe Send nel database.
 * Contiene informazioni su file inviati in modo sicuro, inclusi
 * contenuto criptato, token di accesso, scadenza e altre proprietà.
 * Mappa la tabella "safe_send_entries".
 * Usata da SafeSendRepository per operazioni CRUD.
 * Spring Data JPA si occupa di generare il SQL dietro le quinte.
 */

@Entity
@Table(name = "safe_send_entries")
public class SafeSendEntryEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Indica se il file può essere scaricato una sola volta
    @Column(name = "one_time", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean oneTime = false;

    @Column(name = "encrypted_content", nullable = false, columnDefinition = "BYTEA")
    private byte[] encryptedContent;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "access_count")
    private int accessCount = 0;

    // Relazione Many-to-One con UserEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public SafeSendEntryEntity() {
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public boolean isOneTime() { return oneTime; }
    public void setOneTime(boolean oneTime) { this.oneTime = oneTime; }

    public byte[] getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(byte[] encryptedContent) { this.encryptedContent = encryptedContent; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }
}