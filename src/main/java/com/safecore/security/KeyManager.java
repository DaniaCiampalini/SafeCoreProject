package com.safecore.security;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

/**
 * Gestore delle chiavi di crittografia.
 * In questo sistema, si utilizza il sistema PBKDF2 per derivare la chiave a partire
 * dalla Master Password e dal valore salt memorizzato per l'utente.
 */


@Component
public class KeyManager {

    private SecretKey secretKey;

    public KeyManager() { }

    public void initialize (String masterPassword, byte[] salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(masterPassword.toCharArray(), salt, 65536, 256);

            SecretKey tmp = factory.generateSecret(spec);
            this.secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (Exception e) {
            throw new SecurityException("Impossibile derivare la Master Key. Il vault resterà bloccato.", e);
        }
    }

    public SecretKey getSecretKey() {
        if (this.secretKey == null){
            throw new IllegalStateException("Vault bloccato! Effettua il login per sbloccarlo");
        }

        return secretKey;
    }

    public void clear() { this.secretKey = null; }
}