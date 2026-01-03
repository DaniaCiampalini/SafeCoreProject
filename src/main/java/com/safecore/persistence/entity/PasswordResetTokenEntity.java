package com.safecore.persistence.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Questa classe mappa la tabella dei token di reset.
 * Ho aggiunto @SuppressWarnings("unused") sui campi che JPA usa "sottobanco"
 * così evitiamo warning fastidiosi.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // JPA lo usa per identificare la riga, anche se noi non lo chiamiamo mai nel codice

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    // Risolto warning: JPA ha bisogno di un costruttore vuoto, ma noi lo facciamo protected
    // così nessuno può creare un token "vuoto" per sbaglio fuori dal DB.
    protected PasswordResetTokenEntity() { }

    public PasswordResetTokenEntity(String email, String tokenHash, LocalDateTime expiresAt) {
        this.email = email;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public String getEmail() { return email; }
    public String getTokenHash() { return tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }

    public void markUsed() { this.used = true; }
}
