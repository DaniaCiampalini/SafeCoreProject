package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per ExpiredLinkException.
 * Verifica il comportamento dell'eccezione lanciata quando un link SafeSend è scaduto.
 */
class ExpiredLinkExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Questo link è scaduto";
        ExpiredLinkException exception = new ExpiredLinkException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withExpiredMessage() {
        String message = "Il link SafeSend ha superato il tempo di validità";
        ExpiredLinkException exception = new ExpiredLinkException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withTimestampMessage() {
        String message = "Il link è scaduto il 2026-03-01 alle 10:00";
        ExpiredLinkException exception = new ExpiredLinkException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        ExpiredLinkException exception = new ExpiredLinkException("test");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isThrowable() {
        ExpiredLinkException exception = new ExpiredLinkException("test");

        assertThrows(ExpiredLinkException.class, () -> {
            throw exception;
        });
    }

    @Test
    void messageContainsItalianText() {
        String message = "Link scaduto";
        ExpiredLinkException exception = new ExpiredLinkException(message);

        assertTrue(exception.getMessage().contains("scaduto"));
    }

    @Test
    void canBeCaughtAsRuntimeException() {
        ExpiredLinkException exception = new ExpiredLinkException("test");

        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });
    }
}

