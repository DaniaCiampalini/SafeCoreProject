package com.safecore.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility stateless per hashing e verifica di segreti (password, token).
 *
 * Scelte di Sicurezza:
 * - BCrypt: resistente a brute-force e rainbow tables
 * - Salt automatico gestito dalla libreria
 *
 * Scelte di Ingegneria del Software:
 * - Classe utility (final + costruttore privato)
 * - Nessuno stato interno
 * - Nessuna dipendenza dal dominio o dalla persistence
 *
 * Nota progettuale:
 * - In futuro può essere sostituita da un'interfaccia se emergono
 *   più strategie di hashing (es. Argon2)
 */
public final class PasswordHasher {

    private PasswordHasher() {
        // Utility class → non istanziabile
    }

    /**
     * Hasha un segreto in chiaro (password o token).
     *
     * @param plain valore in chiaro
     * @return hash BCrypt
     */
    public static String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Value to hash cannot be null or blank");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    /**
     * Verifica un valore in chiaro contro un hash BCrypt.
     *
     * @param plain valore in chiaro
     * @param hash hash memorizzato
     * @return true se corrispondono
     */
    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(plain, hash);
    }
}
