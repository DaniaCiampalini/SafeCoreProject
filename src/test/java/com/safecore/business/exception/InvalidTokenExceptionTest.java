package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per InvalidTokenException.
 * Verifica il comportamento dell'eccezione lanciata quando il token SafeSend non è valido.
 */
class InvalidTokenExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Token non valido o link manomesso";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withInvalidTokenMessage() {
        String message = "Il token SafeSend non corrisponde all'hash memorizzato";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withTamperedLinkMessage() {
        String message = "Il link potrebbe essere stato manomesso";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isThrowable() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertThrows(InvalidTokenException.class, () -> {
            throw exception;
        });
    }

    @Test
    void messageContainsItalianText() {
        String message = "Token non valido";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertTrue(exception.getMessage().contains("Token"));
        assertTrue(exception.getMessage().contains("valido"));
    }

    @Test
    void canBeCaughtAsRuntimeException() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });
    }
}

