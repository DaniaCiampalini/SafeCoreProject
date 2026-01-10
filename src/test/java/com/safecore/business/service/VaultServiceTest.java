package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionFactory;
import com.safecore.security.EncryptionStrategy;
import com.safecore.ui.session.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VaultServiceTest {

    private PasswordEntryRepository passwordEntryRepository;
    private UserRepository userRepository;
    private EncryptionFactory encryptionFactory;
    private EncryptionStrategy encryptionStrategy;
    private VaultService vaultService;

    @BeforeEach
    void setUp() {
        passwordEntryRepository = mock(PasswordEntryRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionFactory = mock(EncryptionFactory.class);
        encryptionStrategy = mock(EncryptionStrategy.class);

        when(encryptionFactory.getDefaultStrategy()).thenReturn(encryptionStrategy);

        vaultService = new VaultService(passwordEntryRepository, userRepository, encryptionFactory);
    }

    @Test
    void addEntry_success() {
        String email = "user@test.com";
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(email);

        try (MockedStatic<SessionContext> sessionContext = Mockito.mockStatic(SessionContext.class)) {
            sessionContext.when(SessionContext::getCurrentUserEmail).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
            when(encryptionStrategy.encrypt("plain")).thenReturn("encrypted".getBytes());

            vaultService.addEntry("service", "user", "plain");

            verify(passwordEntryRepository, times(1)).save(any(PasswordEntryEntity.class));
        }
    }

    @Test
    void deleteEntry_success() {
        UUID id = UUID.randomUUID();
        vaultService.deleteEntry(id);
        verify(passwordEntryRepository, times(1)).deleteById(id);
    }
}
