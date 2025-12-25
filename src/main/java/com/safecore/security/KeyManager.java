package com.safecore.security;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/* Singleton responsabile della gestione della chiave master. */
public final class KeyManager {

    private static KeyManager instance;
    private final SecretKey secretKey;

    private KeyManager() {
        byte[] keyBytes = new byte[32]; // 256 bit
        new SecureRandom().nextBytes(keyBytes);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public static synchronized KeyManager getInstance() {
        if (instance == null) {
            instance = new KeyManager();
        }
        return instance;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
