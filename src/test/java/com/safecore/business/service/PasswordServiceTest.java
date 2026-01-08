package com.safecore.business.service;

import com.safecore.business.domain.PasswordEntry;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    private PasswordEntryRepository repository;
    private PasswordService service;

    @BeforeEach
    void setUp() {
        repository = mock(PasswordEntryRepository.class);
        service = new PasswordServiceImpl(repository);
    }

    @Test
    void addCredential_shouldEncryptBeforeSaving() {
        String serviceName = "Netflix";
        String user = "mario";
        String pass = "netflix123";

        service.addCredential(serviceName, user, pass);

        // Catturiamo l'entità che è stata passata al repository per il salvataggio
        ArgumentCaptor<PasswordEntryEntity> captor = ArgumentCaptor.forClass(PasswordEntryEntity.class);
        verify(repository).save(captor.capture());

        PasswordEntryEntity saved = captor.getValue();
        assertEquals(serviceName, saved.getServiceName());
        assertNotEquals(pass, new String(saved.getEncryptedPassword()), "La password nel DB deve essere cifrata!");
    }

    @Test
    void deleteEntry_callsRepository() {
        UUID id = UUID.randomUUID();
        service.deleteEntry(id);
        verify(repository, times(1)).deleteById(id);
    }
}