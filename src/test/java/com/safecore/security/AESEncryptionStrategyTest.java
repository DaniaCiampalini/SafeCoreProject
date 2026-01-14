package com.safecore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AESEncryptionStrategyTest {

    private EncryptionStrategy strategy;

    @BeforeEach
    void setUp() {
        // Creiamo il KeyManager (che ora è un bean/componente)
        KeyManager keyManager = new KeyManager();
        // Lo iniettiamo manualmente nella strategia per il test unitario
        this.strategy = new AESEncryptionStrategy(keyManager);
    }

    @Test
    void testEncryptDecryptSuccess() {
        String originalText = "SecretPassword123!";

        // 1. Cifra
        byte[] encrypted = strategy.encrypt(originalText);
        assertNotNull(encrypted);
        assertNotEquals(originalText, new String(encrypted), "Il testo cifrato non deve essere uguale al chiaro");

        // 2. Decifra
        String decrypted = strategy.decrypt(encrypted);
        assertEquals(originalText, decrypted, "Il testo decifrato deve corrispondere all'originale");
    }

    @Test
    void testEncryptNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> strategy.encrypt(null));
    }

    @Test
    void testDecryptEmptyOrNullThrows() {
        // Nota: avendo messo il controllo if (cipherText == null || length < 16)
        // nel codice della classe, ora deve lanciare IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> strategy.decrypt(null));
        assertThrows(IllegalArgumentException.class, () -> strategy.decrypt(new byte[0]));
    }
}