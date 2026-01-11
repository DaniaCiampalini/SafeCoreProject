package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per WeakPasswordException.
 * Verifica il comportamento dell'eccezione lanciata quando la password è troppo debole.
 */
class WeakPasswordExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "La password è troppo debole";
        WeakPasswordException exception = new WeakPasswordException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withDetailedMessage() {
        String message = "La password deve contenere almeno 8 caratteri, una maiuscola e un numero";
        WeakPasswordException exception = new WeakPasswordException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withEmptyMessage() {
        String message = "";
        WeakPasswordException exception = new WeakPasswordException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withNullMessage() {
        WeakPasswordException exception = new WeakPasswordException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        WeakPasswordException exception = new WeakPasswordException("test");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isRuntimeException() {
        WeakPasswordException exception = new WeakPasswordException("test");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String message = "Password troppo corta";

        assertThrows(WeakPasswordException.class, () -> {
            throw new WeakPasswordException(message);
        });
    }

    @Test
    void exception_hasNoCauseByDefault() {
        WeakPasswordException exception = new WeakPasswordException("test");

        assertNull(exception.getCause());
    }

    @Test
    void exception_canBeCaughtAsSafeCoreException() {
        WeakPasswordException weakPasswordException = new WeakPasswordException("test");

        SafeCoreException caught = weakPasswordException;

        assertNotNull(caught);
        assertEquals("test", caught.getMessage());
    }

    @Test
    void exception_canBeCaughtAsRuntimeException() {
        WeakPasswordException weakPasswordException = new WeakPasswordException("test");

        RuntimeException caught = weakPasswordException;

        assertNotNull(caught);
        assertEquals("test", caught.getMessage());
    }

    @Test
    void multipleExceptionsHaveDifferentMessages() {
        WeakPasswordException exception1 = new WeakPasswordException("Password troppo corta");
        WeakPasswordException exception2 = new WeakPasswordException("Password senza numeri");

        assertEquals("Password troppo corta", exception1.getMessage());
        assertEquals("Password senza numeri", exception2.getMessage());
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    void exceptionWithLongMessage() {
        String longMessage = "La password non soddisfa i requisiti minimi: deve contenere almeno 12 caratteri, " +
                            "inclusa almeno una lettera maiuscola, una lettera minuscola, un numero e " +
                            "un carattere speciale tra !@#$%^&*";
        WeakPasswordException exception = new WeakPasswordException(longMessage);

        assertEquals(longMessage, exception.getMessage());
    }

    @Test
    void exceptionWithSpecialCharactersInMessage() {
        String message = "La password deve contenere: !@#$%^&*()_+-=[]{}|;:,.<>?";
        WeakPasswordException exception = new WeakPasswordException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exceptionWithUnicodeCharacters() {
        String message = "La password deve contenere caratteri speciali: àèìòù ñ ç";
        WeakPasswordException exception = new WeakPasswordException(message);

        assertEquals(message, exception.getMessage());
    }
}
