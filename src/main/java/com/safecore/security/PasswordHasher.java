package com.safecore.security;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    public String hash(String plain) {
        if (plain == null || plain.isBlank()) {
            throw new IllegalArgumentException("Value to hash cannot be empty");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(12)); // Forza 12 per bilanciare sicurezza e performance
    }

    public boolean verify(String plain, String hash) {
        if (plain == null || hash == null) return false;
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
