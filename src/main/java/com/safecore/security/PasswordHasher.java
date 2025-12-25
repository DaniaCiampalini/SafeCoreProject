package com.safecore.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility per hashing e verifica password.
 *
 * Scelte di sicurezza:
 * - bcrypt → resistente a brute-force
 * - salt automatico
 *
 * Scelte SE:
 * - Classe stateless
 * - Nessuna dipendenza dal resto dell'app
 */
public final class PasswordHasher {

    private PasswordHasher() {
        // Utility class → non istanziabile
    }

    /**
     * Hasha una password in chiaro.
     */
    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifica una password in chiaro contro un hash.
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}


