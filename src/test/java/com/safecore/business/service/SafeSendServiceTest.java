package com.safecore.business.service;

import com.safecore.business.service.impl.SafeSendServiceImpl;
import com.safecore.persistence.entity.SafeSendEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.SafeSendRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionStrategy;
import com.safecore.security.PasswordHasher;
import com.safecore.ui.session.SessionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SafeSendServiceTest {

    private SafeSendService service;
    private SafeSendRepository safeSendRepository;
    private UserRepository userRepository;
    private EncryptionStrategy encryptionStrategy;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        safeSendRepository = mock(SafeSendRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionStrategy = mock(EncryptionStrategy.class);
        passwordHasher = mock(PasswordHasher.class);

        // Inizializziamo l'interfaccia con l'impl che ora risiede nel package .impl
        service = new SafeSendServiceImpl(
                safeSendRepository,
                userRepository,
                encryptionStrategy,
                passwordHasher
        );

        // Simula utente loggato
        SessionContext.login("test@example.com");
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    // --- TEST DI CREAZIONE ---
    @Test
    void createSafeLink_generatesTokenAndEncryptsContent() {
        String content = "Secret message";
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        SafeSendEntryEntity savedEntry = new SafeSendEntryEntity();
        savedEntry.setId(UUID.randomUUID());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(content)).thenReturn("encrypted".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hashed-token");
        when(safeSendRepository.save(any(SafeSendEntryEntity.class))).thenReturn(savedEntry);

        String link = service.createSafeLink(content, 24);

        assertNotNull(link);
        assertTrue(link.contains("?t="));
        verify(safeSendRepository).save(any(SafeSendEntryEntity.class));
    }

    @Test
    void createSafeLink_setsOneTimeFlag() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(anyString())).thenReturn("enc".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hash");

        SafeSendEntryEntity saved = new SafeSendEntryEntity();
        saved.setId(UUID.randomUUID());
        when(safeSendRepository.save(any())).thenReturn(saved);

        service.createSafeLink("content", 1);

        // Verifica che il flag monouso sia impostato a true
        verify(safeSendRepository).save(argThat(SafeSendEntryEntity::isOneTime));
    }

    @Test
    void createSafeLink_setsExpirationTime() {
        int hours = 48;
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(safeSendRepository.save(any())).thenReturn(new SafeSendEntryEntity());

        LocalDateTime before = LocalDateTime.now().plusHours(hours).minusSeconds(5);
        service.createSafeLink("content", hours);
        LocalDateTime after = LocalDateTime.now().plusHours(hours).plusSeconds(5);

        verify(safeSendRepository).save(argThat(entry ->
                entry.getExpiresAt().isAfter(before) && entry.getExpiresAt().isBefore(after)
        ));
    }

    // --- TEST DI ACCESSO E SICUREZZA ---

    @Test
    void accessSafeLink_withValidToken_decryptsAndDeletesEntry() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = createValidEntry(id, "enc".getBytes(), "hash");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify("token", "hash")).thenReturn(true);
        when(encryptionStrategy.decrypt(any())).thenReturn("clear-text");

        String result = service.accessSafeLink(id, "token");

        assertEquals("clear-text", result);
        verify(safeSendRepository).delete(entry); // <--- FONDAMENTALE PER MONOUSO
    }

    @Test
    void accessSafeLink_deletesEntryAfterAccess_ensuresOneTimeUse() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = createValidEntry(id, "enc".getBytes(), "hash");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(anyString(), anyString())).thenReturn(true);
        when(encryptionStrategy.decrypt(any())).thenReturn("content");

        service.accessSafeLink(id, "token");

        // Verifica la distruzione immediata dopo il primo accesso
        verify(safeSendRepository).delete(entry);
    }

    @Test
    void accessSafeLink_withExpiredLink_throwsExceptionAndDeletes() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));

        assertThrows(RuntimeException.class, () -> service.accessSafeLink(id, "token"));
        verify(safeSendRepository).delete(entry); // Cancella anche se scaduto per pulizia
    }

    @Test
    void accessSafeLink_withInvalidToken_throwsException() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = createValidEntry(id, "enc".getBytes(), "hash");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify("wrong", "hash")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.accessSafeLink(id, "wrong"));
        verify(safeSendRepository, never()).delete(any()); // Non cancella se il token è solo sbagliato (previene DoS)
    }

    // Helper per creare entry valide rapidamente
    private SafeSendEntryEntity createValidEntry(UUID id, byte[] content, String hash) {
        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setId(id);
        entry.setEncryptedContent(content);
        entry.setTokenHash(hash);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setOneTime(true);
        return entry;
    }
}