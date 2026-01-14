package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.repository.PasswordResetTokenRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private PasswordHasher passwordHasher;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        service = new PasswordResetServiceImpl(userRepository, tokenRepository, passwordHasher);
    }

    @Test
    void resetPassword_success() {
        String email = "utente@test.it";
        String token = "token-puro";
        String tokenHash = "hash-del-token";

        PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
        tokenEntity.setEmail(email);
        tokenEntity.setTokenHash(tokenHash);
        tokenEntity.setUsed(false);
        tokenEntity.setExpiryDate(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByEmailAndUsedFalse(email)).thenReturn(Optional.of(tokenEntity));
        when(passwordHasher.verify(token, tokenHash)).thenReturn(true);
        when(passwordHasher.hash("NuovaPass123!")).thenReturn("new-hash");

        assertDoesNotThrow(() -> service.resetPassword(email, token, "NuovaPass123!"));
        verify(userRepository).updatePassword(email, "new-hash");
    }
}