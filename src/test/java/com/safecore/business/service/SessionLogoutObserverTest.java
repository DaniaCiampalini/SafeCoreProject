package com.safecore.business.service;

import com.safecore.ui.session.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SessionLogoutObserverTest {

    private SessionLogoutObserver observer;

    @BeforeEach
    void setUp() {
        observer = new SessionLogoutObserver();
        // Simula un utente loggato
        SessionContext.login("test@example.com");
    }

    @AfterEach
    void tearDown() {
        // Pulisce la sessione dopo ogni test
        SessionContext.logout();
    }

    @Test
    void onPasswordResetCompleted_invalidatesSession() {
        assertTrue(SessionContext.isLoggedIn());
        assertEquals("test@example.com", SessionContext.getCurrentUserEmail());

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(
                "test@example.com",
                LocalDateTime.now()
        );

        observer.onPasswordResetCompleted(event);

        assertFalse(SessionContext.isLoggedIn());
    }

    @Test
    void onPasswordResetCompleted_forDifferentUser_doesNotInvalidateSession() {
        assertTrue(SessionContext.isLoggedIn());
        assertEquals("test@example.com", SessionContext.getCurrentUserEmail());

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(
                "other@example.com",
                LocalDateTime.now()
        );

        observer.onPasswordResetCompleted(event);

        // La sessione deve rimanere attiva perché il reset è per un altro utente
        assertTrue(SessionContext.isLoggedIn());
        assertEquals("test@example.com", SessionContext.getCurrentUserEmail());
    }

    @Test
    void onPasswordResetCompleted_whenNoActiveSession_doesNotThrow() {
        SessionContext.logout();
        assertFalse(SessionContext.isLoggedIn());

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(
                "test@example.com",
                LocalDateTime.now()
        );

        assertDoesNotThrow(() -> observer.onPasswordResetCompleted(event));
    }

    @Test
    void onPasswordResetCompleted_withNullEvent_throwsException() {
        assertThrows(NullPointerException.class, () ->
                observer.onPasswordResetCompleted(null)
        );
    }

    @Test
    void onPasswordResetCompleted_multipleCallsForSameUser_invalidatesOnlyOnce() {
        assertTrue(SessionContext.isLoggedIn());

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(
                "test@example.com",
                LocalDateTime.now()
        );

        observer.onPasswordResetCompleted(event);
        assertFalse(SessionContext.isLoggedIn());

        // Seconda chiamata non dovrebbe lanciare eccezione
        assertDoesNotThrow(() -> observer.onPasswordResetCompleted(event));
    }

    @Test
    void onPasswordResetCompleted_caseInsensitiveEmailMatch() {
        SessionContext.logout();
        SessionContext.login("Test@Example.COM");

        PasswordResetCompletedEvent event = new PasswordResetCompletedEvent(
                "test@example.com",
                LocalDateTime.now()
        );

        observer.onPasswordResetCompleted(event);

        // Deve invalidare anche con case diverso
        assertFalse(SessionContext.isLoggedIn());
    }
}
