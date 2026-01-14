package com.safecore.business.service;

import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private PasswordResetServiceImpl service;
    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordHasher passwordHasher;
    private PasswordResetEventPublisher eventPublisher;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        eventPublisher = mock(PasswordResetEventPublisher.class);

        service = new PasswordResetServiceImpl(
                userRepository,
                tokenRepository,
                passwordHasher,
                eventPublisher
        );
    }

    @Test
    void requestReset_whenUserExists_generatesTokenAndReturnsResult() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-token");
        when(tokenRepository.save(any(PasswordResetTokenEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetRequestResult result = service.requestReset(email);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertNotNull(result.getExpiresAt());
        assertTrue(result.getExpiresAt().isAfter(LocalDateTime.now()));
        verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void requestReset_whenUserNotExists_throwsUserNotFoundException() {
        String email = "nonexistent@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> service.requestReset(email));
        verify(tokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_withValidToken_updatesPasswordAndMarksTokenUsed() {
        String email = "test@example.com";
        String token = "valid-token";
        String newPassword = "newPassword123";
        String hashedPassword = "hashed-new-password";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash("hashed-token");
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        tokenEntity.setUsed(false);

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(true);
        when(passwordHasher.hash(newPassword)).thenReturn(hashedPassword);

        service.resetPassword(email, token, newPassword);

        verify(userRepository).updatePassword(email, hashedPassword);
        verify(tokenRepository).save(tokenEntity);
        assertTrue(tokenEntity.isUsed());
    }

    @Test
    void resetPassword_withExpiredToken_throwsInvalidTokenException() {
        String email = "test@example.com";
        String token = "expired-token";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash("hashed-token");
        tokenEntity.setExpiryDate(LocalDateTime.now().minusMinutes(10));
        tokenEntity.setUsed(false);

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> service.resetPassword(email, token, "newPassword")
        );

        assertEquals("Il token è scaduto", exception.getMessage());
        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void resetPassword_withInvalidToken_throwsInvalidTokenException() {
        String email = "test@example.com";
        String token = "wrong-token";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash("hashed-token");
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        tokenEntity.setUsed(false);

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(false);

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> service.resetPassword(email, token, "newPassword")
        );

        assertEquals("Token non valido", exception.getMessage());
        verify(userRepository, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void resetPassword_withNoActiveToken_throwsInvalidTokenException() {
        String email = "test@example.com";
        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> service.resetPassword(email, "any-token", "newPassword")
        );

        assertEquals("Nessun token attivo trovato", exception.getMessage());
    }

    @Test
    void resetPassword_publishesEventAfterCommit_whenTransactionActive() {
        String email = "test@example.com";
        String token = "valid-token";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash("hashed-token");
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        tokenEntity.setUsed(false);

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(true);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");

        // Simula transazione attiva
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        try {
            service.resetPassword(email, token, "newPassword");

            // Verifica che l'evento non sia stato pubblicato immediatamente
            verify(eventPublisher, never()).publish(any());

            // Simula commit della transazione
            TransactionSynchronizationManager.getSynchronizations().forEach(
                    TransactionSynchronization::afterCommit
            );

            // Ora l'evento deve essere stato pubblicato
            verify(eventPublisher).publish(any(PasswordResetCompletedEvent.class));
        } finally {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void resetPassword_publishesEventImmediately_whenNoTransaction() {
        String email = "test@example.com";
        String token = "valid-token";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash("hashed-token");
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        tokenEntity.setUsed(false);

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(true);
        when(passwordHasher.hash(anyString())).thenReturn("hashed");

        // Nessuna transazione attiva
        TransactionSynchronizationManager.setActualTransactionActive(false);

        service.resetPassword(email, token, "newPassword");

        // L'evento deve essere pubblicato immediatamente
        verify(eventPublisher).publish(any(PasswordResetCompletedEvent.class));
    }
}
