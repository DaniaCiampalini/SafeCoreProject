package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per SafeCoreException.
 * Verifica il comportamento base dell'eccezione madre di tutte le eccezioni personalizzate.
 */
class SafeCoreExceptionTest {

    @Test
    void constructor_withMessage_setsMessage() {
        String message = "Test exception message";
        SafeCoreException exception = new SafeCoreException(message) {
        };

        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_withEmptyMessage_setsEmptyMessage() {
        String message = "";
        SafeCoreException exception = new SafeCoreException(message) {
        };

        assertEquals(message, exception.getMessage());
    }

    @Test
    void exception_isRuntimeException() {
        SafeCoreException exception = new SafeCoreException("test") {
        };

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String message = "Test exception";

        assertThrows(SafeCoreException.class, () -> {
            throw new SafeCoreException(message) {
            };
        });
    }

    @Test
    void exception_withNullMessage_setsNullMessage() {
        SafeCoreException exception = new SafeCoreException(null) {
        };

        assertNull(exception.getMessage());
    }

    @Test
    void exception_hasNoCauseByDefault() {
        SafeCoreException exception = new SafeCoreException("test") {
        };

        assertNull(exception.getCause());
    }

    @Test
    void exception_canBeCreatedMultipleTimes() {
        SafeCoreException exception1 = new SafeCoreException("message1") {
        };
        SafeCoreException exception2 = new SafeCoreException("message2") {
        };

        assertEquals("message1", exception1.getMessage());
        assertEquals("message2", exception2.getMessage());
        assertNotSame(exception1, exception2);
    }
}
