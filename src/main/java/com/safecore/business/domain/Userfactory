package com.safecore.business.domain;

/**
 * Factory di dominio per User.
 *
 * Scelte SE:
 * - Centralizza la creazione
 * - Rispetta il costruttore package-private
 * - Usabile da service e test
 */
public final class UserFactory {

    private UserFactory() {
    }

    public static User createNew(
            Long id,
            String email,
            String passwordHash,
            boolean mfaEnabled
    ) {
        return new User(id, email, passwordHash, mfaEnabled);
    }
}
