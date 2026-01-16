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
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test di unità per SafeSendService.
 * Verifica la logica di cifratura, creazione link, scadenza e auto-distruzione.
 */
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

        service = new SafeSendServiceImpl(
                safeSendRepository,
                userRepository,
                encryptionStrategy,
                passwordHasher
        );

        // Simuliamo un utente loggato globalmente per il thread del test
        SessionContext.login("test@example.com");
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    // --- SEZIONE: CREAZIONE LINK ---

    @Test
    void createSafeLink_success_generatesSecureUrl() {
        // Arrange
        String secretContent = "MioSegreto123";
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");

        SafeSendEntryEntity savedEntry = new SafeSendEntryEntity();
        savedEntry.setId(UUID.randomUUID());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(secretContent)).thenReturn("cifrato".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hashed-token");
        when(safeSendRepository.save(any(SafeSendEntryEntity.class))).thenReturn(savedEntry);

        // Act
        String link = service.createSafeLink(secretContent, 24);

        // Assert
        assertNotNull(link);
        assertTrue(link.startsWith("https://safecore.io/send/"));
        assertTrue(link.contains("?t="));

        verify(encryptionStrategy).encrypt(secretContent);
        verify(safeSendRepository).save(any(SafeSendEntryEntity.class));
    }

    @Test
    void createSafeLink_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.createSafeLink("content", 1)
        );
    }

    @Test
    void createSafeLink_verifyEntityStateBeforeSave() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encrypt(anyString())).thenReturn("bytes".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("token-hash");

        // Usiamo un Captor per ispezionare l'oggetto passato al repository
        ArgumentCaptor<SafeSendEntryEntity> entryCaptor = ArgumentCaptor.forClass(SafeSendEntryEntity.class);
        when(safeSendRepository.save(entryCaptor.capture())).thenReturn(new SafeSendEntryEntity());

        service.createSafeLink("content", 10);

        SafeSendEntryEntity captured = entryCaptor.getValue();
        assertTrue(captured.isOneTime(), "Il link deve essere impostato come monouso");
        assertEquals(user, captured.getUser(), "L'utente creatore deve essere impostato");
        assertNotNull(captured.getExpiresAt());
        assertEquals("token-hash", captured.getTokenHash());
    }

    // --- SEZIONE: ACCESSO E SICUREZZA ---

    @Test
    void accessSafeLink_success_returnsClearTextAndDeletes() {
        // Arrange
        UUID id = UUID.randomUUID();
        String rawToken = "raw-secret-token";
        String hashedToken = "hashed-version";
        byte[] encryptedBody = "encrypted-payload".getBytes();

        SafeSendEntryEntity entry = createValidEntry(id, encryptedBody, hashedToken);

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(rawToken, hashedToken)).thenReturn(true);
        when(encryptionStrategy.decrypt(encryptedBody)).thenReturn("Messaggio Decifrato");

        // Act
        String result = service.accessSafeLink(id, rawToken);

        // Assert
        assertEquals("Messaggio Decifrato", result);
        verify(safeSendRepository).delete(entry); // Verifica distruzione post-lettura
    }

    @Test
    void accessSafeLink_fails_whenExpired() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Scaduto da 5 min

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, "any-token")
        );

        assertTrue(ex.getMessage().contains("scaduto"));
        verify(safeSendRepository).delete(entry); // Pulizia automatica se scaduto
    }

    @Test
    void accessSafeLink_fails_withWrongToken() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = createValidEntry(id, "data".getBytes(), "correct-hash");

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify("wrong-token", "correct-hash")).thenReturn(false);

        assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, "wrong-token")
        );

        // Non dobbiamo cancellare se il token è solo sbagliato (evita attacchi DoS)
        verify(safeSendRepository, never()).delete(any());
    }

    @Test
    void accessSafeLink_fails_whenEntryNotFound() {
        UUID id = UUID.randomUUID();
        when(safeSendRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                service.accessSafeLink(id, "token")
        );
    }

    // --- HELPER METHODS ---

    /**
     * Helper per creare rapidamente una entry valida per i test di accesso.
     */
    private SafeSendEntryEntity createValidEntry(UUID id, byte[] content, String tokenHash) {
        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setId(id);
        entry.setEncryptedContent(content);
        entry.setTokenHash(tokenHash);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setOneTime(true);
        entry.setAccessCount(0);
        return entry;
    }
}