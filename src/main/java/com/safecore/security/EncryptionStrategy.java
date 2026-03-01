package com.safecore.security;

/**
 * Interfaccia per le strategie di crittografia.
 * Implementazioni concrete possono essere AES, RSA, ecc.
 */

public interface EncryptionStrategy {
    byte[] encrypt(String plaintext);
    String decrypt(byte[] ciphertext);

    byte[] encryptWithToken(String content, String token);
    String decryptWithToken(byte[] cipherText, String token);
}