package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per LinkNotFoundException.
 * Verifica il comportamento dell'eccezione lanciata quando un link SafeSend non viene trovato.
 */
class LinkNotFoundExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Il link non esiste più o è stato già usato";
        LinkNotFoundException exception = new LinkNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withNotFoundMessage() {
        String message = "Link SafeSend non trovato nel database";
        LinkNotFoundException exception = new LinkNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withAlreadyUsedMessage() {
        String message = "Questo link è stato già utilizzato (burn-after-reading)";
        LinkNotFoundException exception = new LinkNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        LinkNotFoundException exception = new LinkNotFoundException("test");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isThrowable() {
        LinkNotFoundException exception = new LinkNotFoundException("test");

        assertThrows(LinkNotFoundException.class, () -> {
            throw exception;
        });
    }

    @Test
    void messageContainsItalianText() {
        String message = "Link non trovato";
        LinkNotFoundException exception = new LinkNotFoundException(message);

        assertTrue(exception.getMessage().contains("Link"));
        assertTrue(exception.getMessage().contains("trovato"));
    }

    @Test
    void canBeCaughtAsRuntimeException() {
        LinkNotFoundException exception = new LinkNotFoundException("test");

        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });
    }

    @Test
    void messageCanIndicateBurnAfterReading() {
        String message = "Link già utilizzato (monouso)";
        LinkNotFoundException exception = new LinkNotFoundException(message);

        assertTrue(exception.getMessage().contains("utilizzato") ||
                   exception.getMessage().contains("monouso"));
    }
}

