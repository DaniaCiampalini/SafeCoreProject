package com.safecore.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AESEncryptionStrategyTest {

    private final EncryptionStrategy strategy = new AESEncryptionStrategy();

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
    void testDecryptEmptyOrNullReturnsEmpty() {
        assertEquals("", strategy.decrypt(null));
        assertEquals("", strategy.decrypt(new byte[0]));
    }
}