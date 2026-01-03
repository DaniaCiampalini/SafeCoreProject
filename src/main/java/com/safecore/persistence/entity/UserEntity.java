package com.safecore.persistence.entity;

import javax.persistence.*;
import java.util.UUID;

/**
 * Entity JPA.
 *
 * Responsabilità:
 * - Mappare la tabella DB
 * - Nessuna logica di business
 *
 * NOTA:
 * - È mutabile (JPA lo richiede)
 * - Separata dal Domain Model
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean mfaEnabled;

    // Costruttore vuoto obbligatorio per JPA
    public UserEntity() {}

    // Getter e Setter (mutabilità richiesta da Hibernate)
    public UUID getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isMfaEnabled() {
        return mfaEnabled;
    }

    public void setMfaEnabled(boolean mfaEnabled) {
        this.mfaEnabled = mfaEnabled;
    }
}
