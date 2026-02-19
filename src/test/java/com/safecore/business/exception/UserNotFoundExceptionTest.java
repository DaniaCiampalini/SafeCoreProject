package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per UserNotFoundException.
 * Verifica il comportamento dell'eccezione lanciata quando un utente non viene trovato.
 */
class UserNotFoundExceptionTest {

    @Test
    void constructor_withEmail_setsFormattedMessage() {
        String email = "test@example.com";
        UserNotFoundException exception = new UserNotFoundException(email);

        String expectedMessage = "Spiacente, non esiste nessun utente con l'email: " + email;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_withEmptyEmail_setsFormattedMessage() {
        String email = "";
        UserNotFoundException exception = new UserNotFoundException(email);

        String expectedMessage = "Spiacente, non esiste nessun utente con l'email: " + email;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_withNullEmail_setsFormattedMessage() {
        String email = null;
        UserNotFoundException exception = new UserNotFoundException(email);

        String expectedMessage = "Spiacente, non esiste nessun utente con l'email: null";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        UserNotFoundException exception = new UserNotFoundException("test@example.com");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isRuntimeException() {
        UserNotFoundException exception = new UserNotFoundException("test@example.com");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String email = "nonexistent@example.com";

        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(email);
        });
    }

    @Test
    void exception_hasNoCauseByDefault() {
        UserNotFoundException exception = new UserNotFoundException("test@example.com");

        assertNull(exception.getCause());
    }

    @Test
    void exception_canBeCaughtAsSafeCoreException() {
        UserNotFoundException userNotFoundException = new UserNotFoundException("test@example.com");

        SafeCoreException caught = userNotFoundException;

        assertNotNull(caught);
        assertTrue(caught.getMessage().contains("test@example.com"));
    }

    @Test
    void exception_canBeCaughtAsRuntimeException() {
        UserNotFoundException userNotFoundException = new UserNotFoundException("test@example.com");

        RuntimeException caught = userNotFoundException;

        assertNotNull(caught);
        assertTrue(caught.getMessage().contains("test@example.com"));
    }

    @Test
    void messageContainsEmail() {
        String email = "user@domain.com";
        UserNotFoundException exception = new UserNotFoundException(email);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void messageContainsItalianText() {
        UserNotFoundException exception = new UserNotFoundException("test@example.com");

        assertTrue(exception.getMessage().contains("Spiacente"));
        assertTrue(exception.getMessage().contains("non esiste"));
    }

    @Test
    void multipleExceptionsHaveDifferentMessages() {
        UserNotFoundException exception1 = new UserNotFoundException("user1@example.com");
        UserNotFoundException exception2 = new UserNotFoundException("user2@example.com");

        assertTrue(exception1.getMessage().contains("user1@example.com"));
        assertTrue(exception2.getMessage().contains("user2@example.com"));
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    void exceptionWithSpecialCharactersInEmail() {
        String email = "user+tag@sub.domain.co.uk";
        UserNotFoundException exception = new UserNotFoundException(email);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void exceptionWithLongEmail() {
        String email = "very.long.email.address.with.many.dots@very.long.domain.name.com";
        UserNotFoundException exception = new UserNotFoundException(email);

        assertTrue(exception.getMessage().contains(email));
    }
}
