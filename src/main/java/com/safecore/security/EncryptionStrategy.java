package com.safecore.security;

/* Strategy per algoritmi di cifratura -> si può cambiare algoritmo senza modificare codice client. */
public interface EncryptionStrategy {

    byte[] encrypt(String plaintext);
    String decrypt(byte[] ciphertext);
}
 //rispetta principio O/C