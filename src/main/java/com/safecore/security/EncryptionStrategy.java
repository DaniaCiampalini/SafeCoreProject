package com.safecore.security;

/**
 * Interfaccia per le strategie di crittografia.
 * Implementazioni concrete possono essere AES, RSA, ecc.
 */

public interface EncryptionStrategy {
    byte[] encrypt(String plaintext);

    String decrypt(byte[] ciphertext);
}