package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        // Mocking: sostituisce i vecchi FakeDao
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        service = new PasswordResetServiceImpl(userRepository, tokenRepository);
    }

    @Test
    void resetPassword_success() {
        String email = "utente@test.it";
        String token = "token-valido";

        // Creiamo un'entità token finta per il mock
        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setUsed(false);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        // Simuliamo l'hash del token (deve corrispondere a quello generato dal service o saltiamo il check nel test)

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));

        assertDoesNotThrow(() -> service.resetPassword(email, token, "NuovaPass123!"));
    }

    @Test
    void requestReset_failsIfUserNotFound() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                service.requestReset("non-esiste@test.it"));
    }
}