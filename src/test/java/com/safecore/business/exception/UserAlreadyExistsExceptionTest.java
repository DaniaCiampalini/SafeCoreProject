package com.safecore.business.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per UserAlreadyExistsException.
 * Verifica il comportamento dell'eccezione lanciata quando si tenta di registrare un utente già esistente.
 */
class UserAlreadyExistsExceptionTest {

    @Test
    void constructor_withEmail_setsFormattedMessage() {
        String email = "test@example.com";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        String expectedMessage = "Un account con l'email " + email + " esiste già. Prova a fare il login.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_withEmptyEmail_setsFormattedMessage() {
        String email = "";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        String expectedMessage = "Un account con l'email " + email + " esiste già. Prova a fare il login.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void constructor_withNullEmail_setsFormattedMessage() {
        String email = null;
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        String expectedMessage = "Un account con l'email null esiste già. Prova a fare il login.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void exception_isSafeCoreException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertTrue(exception instanceof SafeCoreException);
    }

    @Test
    void exception_isRuntimeException() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void exception_canBeThrownAndCaught() {
        String email = "existing@example.com";

        assertThrows(UserAlreadyExistsException.class, () -> {
            throw new UserAlreadyExistsException(email);
        });
    }

    @Test
    void exception_hasNoCauseByDefault() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertNull(exception.getCause());
    }

    @Test
    void exception_canBeCaughtAsSafeCoreException() {
        UserAlreadyExistsException userAlreadyExistsException = new UserAlreadyExistsException("test@example.com");

        SafeCoreException caught = userAlreadyExistsException;

        assertNotNull(caught);
        assertTrue(caught.getMessage().contains("test@example.com"));
    }

    @Test
    void exception_canBeCaughtAsRuntimeException() {
        UserAlreadyExistsException userAlreadyExistsException = new UserAlreadyExistsException("test@example.com");

        RuntimeException caught = userAlreadyExistsException;

        assertNotNull(caught);
        assertTrue(caught.getMessage().contains("test@example.com"));
    }

    @Test
    void messageContainsEmail() {
        String email = "user@domain.com";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void messageContainsItalianText() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        assertTrue(exception.getMessage().contains("Un account"));
        assertTrue(exception.getMessage().contains("esiste già"));
        assertTrue(exception.getMessage().contains("Prova a fare il login"));
    }

    @Test
    void multipleExceptionsHaveDifferentMessages() {
        UserAlreadyExistsException exception1 = new UserAlreadyExistsException("user1@example.com");
        UserAlreadyExistsException exception2 = new UserAlreadyExistsException("user2@example.com");

        assertTrue(exception1.getMessage().contains("user1@example.com"));
        assertTrue(exception2.getMessage().contains("user2@example.com"));
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    void exceptionWithSpecialCharactersInEmail() {
        String email = "user+tag@sub.domain.co.uk";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void exceptionWithLongEmail() {
        String email = "very.long.email.address.with.many.dots@very.long.domain.name.com";
        UserAlreadyExistsException exception = new UserAlreadyExistsException(email);

        assertTrue(exception.getMessage().contains(email));
    }

    @Test
    void messageIsFriendlyAndHelpful() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException("test@example.com");

        String message = exception.getMessage();
        assertTrue(message.contains("Ehilà") || message.contains("esiste già"));
        assertTrue(message.contains("login"));
    }
}
