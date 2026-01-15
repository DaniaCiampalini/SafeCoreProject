package com.safecore.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "safe_send_entries")
public class SafeSendEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Specifica esplicitamente UUID
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // Usiamo @Lob per supportare segreti di qualsiasi dimensione
    @Lob
    @Column(nullable = false, columnDefinition = "BLOB")
    private byte[] encryptedContent;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // Rinominato per uniformità con il Service e standard Java Bean
    @Column(nullable = false)
    private boolean oneTime = true;

    @Column(nullable = false)
    private int accessCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity creator;

    public SafeSendEntry() {}

    // GETTER E SETTER
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public byte[] getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(byte[] encryptedContent) { this.encryptedContent = encryptedContent; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    // Corretti i nomi dei metodi booleani per il Service
    public boolean isOneTime() { return oneTime; }
    public void setOneTime(boolean oneTime) { this.oneTime = oneTime; }

    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }

    public UserEntity getCreator() { return creator; }
    public void setCreator(UserEntity creator) { this.creator = creator; }
}