package com.safecore.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Gestore delle chiavi di crittografia.
 * In un sistema reale, questa classe si occuperebbe di caricare le chiavi da un Vault sicuro
 * o di generarle in modo sicuro basandosi su una Master Password.
 */


@Component
public class KeyManager {

    private final SecretKey secretKey;

    public KeyManager() {
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}