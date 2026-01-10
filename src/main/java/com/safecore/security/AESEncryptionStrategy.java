package com.safecore.security;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Component
public class AESEncryptionStrategy implements EncryptionStrategy {

    // Usiamo AES in modalità CBC con padding PKCS5. Standard e sicuro.
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final KeyManager keyManager;

    public AESEncryptionStrategy(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Override
    public byte[] encrypt(String plainText) {
        if (plainText == null) throw new IllegalArgumentException("Plaintext cannot be null");
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Generiamo un IV (Initialization Vector) casuale di 16 byte.
            // L'IV serve a far sì che cifrando la stessa password due volte, il risultato sia diverso.
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Prepariamo il cifratore con la chiave che arriva dal KeyManager
            cipher.init(Cipher.ENCRYPT_MODE, keyManager.getSecretKey(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Per poter decifrare dopo, dobbiamo salvarci l'IV. 
            // Lo mettiamo proprio all'inizio del pacchetto di byte finale.
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(byte[] cipherText) {
        // Il ciphertext deve contenere almeno i 16 byte dell'IV
        if (cipherText == null || cipherText.length < 16) throw new IllegalArgumentException("Invalid ciphertext");
        try {
            // Estraiamo l'IV dai primi 16 byte
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[cipherText.length - 16];
            System.arraycopy(cipherText, 0, iv, 0, 16);
            System.arraycopy(cipherText, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyManager.getSecretKey(), new IvParameterSpec(iv));

            // Ed ecco di nuovo il testo in chiaro!
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("Decryption failed", e);
        }
    }
}