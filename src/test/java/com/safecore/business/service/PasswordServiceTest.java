package com.safecore.business.service;

import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.security.EncryptionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class PasswordServiceTest {

    private PasswordEntryRepository repository;
    private EncryptionStrategy encryption;
    private PasswordService service;

    @BeforeEach
    void setUp() {
        repository = mock(PasswordEntryRepository.class);
        encryption = mock(EncryptionStrategy.class);
        service = new PasswordServiceImpl(repository, encryption);
    }

    @Test
    void addCredential_callsEncryption() {
        service.addCredential("Google", "user", "pass123");
        verify(encryption, times(1)).encrypt("pass123");
    }
}