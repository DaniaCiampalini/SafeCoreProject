package com.safecore.security;

import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

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
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keyManager.getSecretKey(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

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
}