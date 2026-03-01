package com.safecore.security;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * Implementazione della strategia di cifratura AES.
 * Usiamo AES in modalità CBC con padding PKCS5. Standard e sicuro.
 * L'Initialization Vector (IV) viene generato casualmente per ogni cifratura
 * e pre-pended al ciphertext per poter essere usato in fase di decifratura.
 */

@Component
public class AESEncryptionStrategy implements EncryptionStrategy {

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

            // L'IV serve a far sì che cifrando la stessa password due volte il risultato sia diverso.
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keyManager.getSecretKey(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Per poter decifrare dopo, dobbiamo salvarci l'IV.
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
        if (cipherText == null || cipherText.length < 16) throw new IllegalArgumentException("Invalid ciphertext");
        try {
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[cipherText.length - 16];
            System.arraycopy(cipherText, 0, iv, 0, 16);
            System.arraycopy(cipherText, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keyManager.getSecretKey(), new IvParameterSpec(iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("Decryption failed", e);
        }
    }

    @Override
    public byte[] encryptWithToken(String content, String token) {
        try {
            // We turn the Token string into a 16-byte key
            SecretKeySpec tokenKey = new SecretKeySpec(token.substring(0, 16).getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, tokenKey, ivSpec);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return result;
        } catch (Exception e) {
            throw new SecurityException("SafeSend encryption failed", e);
        }
    }

    @Override
    public String decryptWithToken(byte[] cipherText, String token) {
        if (cipherText == null || cipherText.length < 16) throw new IllegalArgumentException("Invalid ciphertext");
        try {
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[cipherText.length - 16];
            System.arraycopy(cipherText, 0, iv, 0, 16);
            System.arraycopy(cipherText, 16, encrypted, 0, encrypted.length);

            byte[] keyBytes = token.substring(0, 16).getBytes(StandardCharsets.UTF_8);
            SecretKeySpec tokenKey = new SecretKeySpec(keyBytes, "AES");

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, tokenKey, new IvParameterSpec(iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new SecurityException("SafeSend decryption failed", e);
        }
    }
}