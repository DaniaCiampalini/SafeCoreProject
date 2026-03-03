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
        keyManager.initialize("TestPassword123!", new byte[32]); // Inizializza con password e salt
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

    @Test
    void testencryptDecryptWithToken_Success() {
        String content = "Questo è un segreto SafeSend";
        // Il token deve essere >= 16 caratteri perché usiamo substring(0, 16)
        String token = "this-is-a-very-long-secure-token-123";

        // 1. Cifra usando il token
        byte[] encrypted = strategy.encryptWithToken(content, token);
        assertNotNull(encrypted);
        assertTrue(encrypted.length > 16, "Il risultato deve contenere l'IV (16 byte) + dati");

        // 2. Decifra usando lo stesso token
        String decrypted = strategy.decryptWithToken(encrypted, token);
        assertEquals(content, decrypted, "Il contenuto decifrato deve corrispondere all'originale");
    }

    @Test
    void encryptDecryptWithToken_WrongToken_ThrowsException() {
        String content = "Dati sensibili";
        String rightToken = "correct-token-long-enough";
        String wrongToken = "wrong-token-long-enough-too";

        byte[] encrypted = strategy.encryptWithToken(content, rightToken);

        // La decifratura con il token sbagliato deve lanciare una SecurityException (causata da BadPaddingException)
        assertThrows(SecurityException.class, () -> {
            strategy.decryptWithToken(encrypted, wrongToken);
        });
    }

    @Test
    void encryptWithToken_ProducesDifferentCiphertext() {
        String content = "Test Content";
        String token = "secure-token-12345";

        byte[] encrypted1 = strategy.encryptWithToken(content, token);
        byte[] encrypted2 = strategy.encryptWithToken(content, token);

        // Grazie al SecureRandom IV, due cifrature identiche devono produrre array diversi
        assertFalse(java.util.Arrays.equals(encrypted1, encrypted2));
    }

    @Test
    void decryptWithToken_InvalidInput_Throws() {
        String token = "secure-token-12345";

        // Ciphertext troppo corto (manca l'IV)
        assertThrows(IllegalArgumentException.class, () -> strategy.decryptWithToken(new byte[10], token));
        // Ciphertext nullo
        assertThrows(IllegalArgumentException.class, () -> strategy.decryptWithToken(null, token));
    }
}