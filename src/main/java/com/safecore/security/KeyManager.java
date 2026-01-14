package com.safecore.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

@Component
public class KeyManager {

    private final SecretKey secretKey;

    public KeyManager() {
        // In un sistema reale, questa chiave verrebbe caricata da un Vault esterno o generata da Master Password
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}