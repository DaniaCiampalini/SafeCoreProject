package com.safecore.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

/**
 * Classe per l'hashing e la verifica delle password utilizzando BCrypt.
 */

@Component
public class PasswordHasher {

    public String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Value to hash cannot be empty");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(12)); // 12 è un buon compromesso tra sicurezza e performance
    }

    public boolean verify(String plain, String hash) { // Confronta la password in chiaro con l'hash memorizzato
        if (plain == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
