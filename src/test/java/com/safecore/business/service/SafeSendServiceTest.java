package com.safecore.business.service;

import com.safecore.persistence.entity.SafeSendEntry;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

        service = new SafeSendService(
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

    @Test
    void createSafeLink_generatesTokenAndEncryptsContent() {
        String content = "Secret message";
        int expirationHours = 24;
        byte[] encryptedContent = "encrypted".getBytes();

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");

        SafeSendEntry savedEntry = new SafeSendEntry();
        savedEntry.setId(UUID.randomUUID());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(content)).thenReturn(encryptedContent);
        when(passwordHasher.hash(anyString())).thenReturn("hashed-token");
        when(safeSendRepository.save(any(SafeSendEntry.class))).thenReturn(savedEntry);

        String link = service.createSafeLink(content, expirationHours);

        assertNotNull(link);
        assertTrue(link.contains("https://safecore.io/send/"));
        assertTrue(link.contains("?t="));
        verify(encryptionStrategy).encrypt(content);
        verify(safeSendRepository).save(any(SafeSendEntry.class));
    }

    @Test
    void createSafeLink_setsOneTimeFlag() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");

        SafeSendEntry savedEntry = new SafeSendEntry();
        savedEntry.setId(UUID.randomUUID());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(anyString())).thenReturn("encrypted".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        when(safeSendRepository.save(any(SafeSendEntry.class))).thenReturn(savedEntry);

        service.createSafeLink("content", 1);

        verify(safeSendRepository).save(argThat(entry ->
                entry.isOneTime() == true
        ));
    }

    @Test
    void createSafeLink_setsExpirationTime() {
        int expirationHours = 48;
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");

        SafeSendEntry savedEntry = new SafeSendEntry();
        savedEntry.setId(UUID.randomUUID());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(anyString())).thenReturn("encrypted".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hashed");
        when(safeSendRepository.save(any(SafeSendEntry.class))).thenReturn(savedEntry);

        LocalDateTime before = LocalDateTime.now().plusHours(expirationHours).minusSeconds(5);
        service.createSafeLink("content", expirationHours);
        LocalDateTime after = LocalDateTime.now().plusHours(expirationHours).plusSeconds(5);

        verify(safeSendRepository).save(argThat(entry ->
                entry.getExpiresAt().isAfter(before) && entry.getExpiresAt().isBefore(after)
        ));
    }

    @Test
    void accessSafeLink_withValidToken_decryptsAndDeletesEntry() {
        UUID id = UUID.randomUUID();
        String token = "valid-token";
        String decryptedContent = "Secret message";

        SafeSendEntry entry = new SafeSendEntry();
        entry.setId(id);
        entry.setEncryptedContent("encrypted".getBytes());
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setTokenHash("hashed-token");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(true);
        when(encryptionStrategy.decrypt("encrypted".getBytes())).thenReturn(decryptedContent);

        String result = service.accessSafeLink(id, token);

        assertEquals(decryptedContent, result);
        verify(encryptionStrategy).decrypt("encrypted".getBytes());
        verify(safeSendRepository).delete(entry);
    }

    @Test
    void accessSafeLink_withExpiredLink_throwsException() {
        UUID id = UUID.randomUUID();
        String token = "token";

        SafeSendEntry entry = new SafeSendEntry();
        entry.setId(id);
        entry.setExpiresAt(LocalDateTime.now().minusHours(1));

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, token)
        );

        assertEquals("Questo link è scaduto.", exception.getMessage());
        verify(safeSendRepository).delete(entry);
        verify(encryptionStrategy, never()).decrypt(any());
    }

    @Test
    void accessSafeLink_withInvalidToken_throwsException() {
        UUID id = UUID.randomUUID();
        String token = "wrong-token";

        SafeSendEntry entry = new SafeSendEntry();
        entry.setId(id);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setTokenHash("hashed-token");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, token)
        );

        assertEquals("Token non valido o link manomesso.", exception.getMessage());
        verify(safeSendRepository, never()).delete(any());
        verify(encryptionStrategy, never()).decrypt(any());
    }

    @Test
    void accessSafeLink_withNonExistentId_throwsException() {
        UUID id = UUID.randomUUID();
        String token = "token";

        when(safeSendRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, token)
        );

        assertEquals("Il link non esiste più o è stato già usato.", exception.getMessage());
    }

    @Test
    void accessSafeLink_deletesEntryAfterAccess_ensuresOneTimeUse() {
        UUID id = UUID.randomUUID();
        String token = "valid-token";

        SafeSendEntry entry = new SafeSendEntry();
        entry.setId(id);
        entry.setEncryptedContent("encrypted".getBytes());
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setTokenHash("hashed-token");
        entry.setOneTime(true);

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(token, "hashed-token")).thenReturn(true);
        when(encryptionStrategy.decrypt(any())).thenReturn("content");

        service.accessSafeLink(id, token);

        // Verifica che l'entry sia stata eliminata (uso singolo)
        verify(safeSendRepository).delete(entry);

        // Secondo accesso deve fallire
        when(safeSendRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, token)
        );

        assertEquals("Il link non esiste più o è stato già usato.", exception.getMessage());
    }

    @Test
    void createSafeLink_whenUserNotFound_throwsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.createSafeLink("content", 1)
        );

        assertEquals("Utente non trovato", exception.getMessage());
        verify(safeSendRepository, never()).save(any());
    }

    @Test
    void accessSafeLink_withNullTokenHash_throwsException() {
        UUID id = UUID.randomUUID();
        String token = "token";

        SafeSendEntry entry = new SafeSendEntry();
        entry.setId(id);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setTokenHash(null);

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, token)
        );

        assertEquals("Token non valido o link manomesso.", exception.getMessage());
    }
}
