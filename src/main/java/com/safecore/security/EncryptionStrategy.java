package com.safecore.security;

public interface EncryptionStrategy {
    byte[] encrypt(String plaintext);

    String decrypt(byte[] ciphertext);
}