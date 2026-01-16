package com.safecore.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "safe_send_entries")
public class SafeSendEntryEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Usiamo columnDefinition per garantire un valore di default a livello DB
    // e prevenire l'errore che hai visto se aggiungerai altri record in futuro.
    @Column(name = "one_time", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean oneTime = false;

    @Column(name = "encrypted_content", nullable = false, columnDefinition = "VARBINARY(MAX)")
    private byte[] encryptedContent;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "access_count")
    private int accessCount = 0;

    // Relazione opzionale: un utente può inviare file, o può essere un invio anonimo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public SafeSendEntryEntity() {
    }

    // Getter e Setter
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