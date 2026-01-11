package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per InvalidTokenException.
 * Verifica il comportamento dell'eccezione lanciata quando il token di reset password non è valido.
 */
class InvalidTokenExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Token is invalid or expired";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withExpiredTokenMessage() {
        String message = "Il token di reset è scaduto";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withAlreadyUsedTokenMessage() {
        String message = "Il token di reset è già stato utilizzato";
        InvalidTokenException exception = new InvalidTokenException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isRuntimeException() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String message = "Token non valido";

        assertThrows(InvalidTokenException.class, () -> {
            throw new InvalidTokenException(message);
        });
    }

    @Test
    void exception_withEmptyMessage() {
        InvalidTokenException exception = new InvalidTokenException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void exception_withNullMessage() {
        InvalidTokenException exception = new InvalidTokenException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void exception_hasNoCauseByDefault() {
        InvalidTokenException exception = new InvalidTokenException("test");

        assertNull(exception.getCause());
    }

    @Test
    void exception_canBeCaughtAsSafeCoreException() {
        InvalidTokenException invalidTokenException = new InvalidTokenException("test");

        SafeCoreException caught = invalidTokenException;

        assertNotNull(caught);
        assertEquals("test", caught.getMessage());
    }

    @Test
    void exception_canBeCaughtAsRuntimeException() {
        InvalidTokenException invalidTokenException = new InvalidTokenException("test");

        RuntimeException caught = invalidTokenException;

        assertNotNull(caught);
        assertEquals("test", caught.getMessage());
    }

    @Test
    void multipleExceptionsHaveDifferentMessages() {
        InvalidTokenException exception1 = new InvalidTokenException("Token scaduto");
        InvalidTokenException exception2 = new InvalidTokenException("Token già usato");

        assertEquals("Token scaduto", exception1.getMessage());
        assertEquals("Token già usato", exception2.getMessage());
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }
}
