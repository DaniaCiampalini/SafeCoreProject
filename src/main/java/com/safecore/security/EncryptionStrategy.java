package com.safecore.security;

/**
 * Qui usiamo lo Strategy Pattern.
 * Nota: non abbiamo "fissato" l'algoritmo AES nel codice del Vault.
 * In questo modo, se domani scoprono una vulnerabilità in AES e dobbiamo passare
 * a qualcosa di più moderno, cambiamo solo l'implementazione e non tutto il sistema.
 */
public interface EncryptionStrategy {
    // Trasforma il testo in chiaro in byte cifrati (usato per il salvataggio)
    byte[] encrypt(String plaintext);

    // Trasforma i byte cifrati in testo leggibile (usato per la visualizzazione)
    String decrypt(byte[] ciphertext);
}