package com.safecore.business.service;

import com.safecore.business.exception.ExpiredLinkException;
import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.LinkNotFoundException;
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
import org.junit.jupiter.api.DisplayName;
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
 * Copre i requisiti di business per la condivisione sicura (SafeSend).
 * * Verifiche incluse:
 * - Creazione link sicuri e notifica observers.
 * - Accesso monouso (Burn-after-reading).
 * - Gestione della scadenza temporale.
 * - Protezione contro token errati.
 * * @author Dania Ciampalini
 */
class SafeSendServiceTest {

    private SafeSendService service;

    // Mock delle dipendenze
    private SafeSendRepository safeSendRepository;
    private UserRepository userRepository;
    private EncryptionStrategy encryptionStrategy;
    private PasswordHasher passwordHasher;
    private VaultService vaultService; // Mock fondamentale per il pattern Observer

    @BeforeEach
    void setUp() {
        // Inizializzazione Mockito
        safeSendRepository = mock(SafeSendRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionStrategy = mock(EncryptionStrategy.class);
        passwordHasher = mock(PasswordHasher.class);
        vaultService = mock(VaultService.class);

        // Iniezione dipendenze nel servizio da testare
        service = new SafeSendServiceImpl(
                safeSendRepository,
                userRepository,
                encryptionStrategy,
                passwordHasher,
                vaultService
        );

        // Setup della sessione utente fittizia
        SessionContext.login("tester@safecore.com");
    }

    @AfterEach
    void tearDown() {
        SessionContext.logout();
    }

    // --- TEST CREAZIONE LINK ---

    @Test
    @DisplayName("Creazione link: Successo con generazione URL corretto e notifica UI")
    void createSafeLink_Success() {
        // Arrange
        String content = "Segreto Super Protetto";
        UserEntity user = new UserEntity();
        user.setEmail("tester@safecore.com");

        SafeSendEntryEntity saved = new SafeSendEntryEntity();
        saved.setId(UUID.randomUUID());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encryptWithToken(eq(content), anyString())).thenReturn("encrypted_data".getBytes());
        when(passwordHasher.hash(anyString())).thenReturn("hashed_token");
        when(safeSendRepository.save(any(SafeSendEntryEntity.class))).thenReturn(saved);

        // Act
        String resultLink = service.createSafeLink(content, 48);

        // Assert
        assertNotNull(resultLink);
        assertTrue(resultLink.startsWith("https://safecore.io/send/"));
        assertTrue(resultLink.contains("?t="));

        // Verifica che il pattern Observer sia scattato (fondamentale per 30L)
        verify(vaultService, times(1)).notifyObservers();
        verify(safeSendRepository).save(any(SafeSendEntryEntity.class));
        verify(encryptionStrategy).encryptWithToken(eq(content), anyString());
    }

    @Test
    @DisplayName("Creazione link: Fallimento se l'utente in sessione non esiste nel DB")
    void createSafeLink_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createSafeLink("data", 1));
        verify(safeSendRepository, never()).save(any());
    }

    @Test
    @DisplayName("Creazione link: Verifica correttezza dei dati salvati (Captor)")
    void createSafeLink_VerifyEntityMapping() {
        UserEntity user = new UserEntity();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encryptionStrategy.encryptWithToken(anyString(), anyString())).thenReturn(new byte[0]);
        when(passwordHasher.hash(anyString())).thenReturn("hash");

        ArgumentCaptor<SafeSendEntryEntity> captor = ArgumentCaptor.forClass(SafeSendEntryEntity.class);
        when(safeSendRepository.save(captor.capture())).thenReturn(new SafeSendEntryEntity());

        service.createSafeLink("secret", 5);

        SafeSendEntryEntity captured = captor.getValue();
        assertEquals(user, captured.getUser());
        assertTrue(captured.isOneTime());
        assertNotNull(captured.getExpiresAt());
        // Verifichiamo che la scadenza sia nel futuro (circa 5 ore da ora)
        assertTrue(captured.getExpiresAt().isAfter(LocalDateTime.now().plusHours(4)));
    }

    // --- TEST ACCESSO E CONSUMO ---

    @Test
    @DisplayName("Accesso link: Successo, decifratura e cancellazione immediata (One-Time)")
    void accessSafeLink_Success_AndDeletion() {
        // Arrange
        UUID id = UUID.randomUUID();
        String token = "valid_token";
        String tokenHash = "valid_hash";
        byte[] encryptedData = "data".getBytes();

        SafeSendEntryEntity entry = createEntry(id, encryptedData, tokenHash, 1);

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify(token, tokenHash)).thenReturn(true);
        when(encryptionStrategy.decryptWithToken(encryptedData, token)).thenReturn("Messaggio in chiaro");

        // Act
        String result = service.accessSafeLink(id, token);

        // Assert
        assertEquals("Messaggio in chiaro", result);

        // Verifica il principio "Burn after reading"
        verify(safeSendRepository).delete(entry);
        verify(vaultService).notifyObservers();
        verify(encryptionStrategy).decryptWithToken(encryptedData, token);
    }

    @Test
    @DisplayName("Accesso link: Fallimento e cancellazione se il link è scaduto")
    void accessSafeLink_Expired() {
        UUID id = UUID.randomUUID();
        SafeSendEntryEntity entry = createEntry(id, new byte[0], "hash", -1); // Scaduto

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));

        ExpiredLinkException ex = assertThrows(ExpiredLinkException.class, () ->
                service.accessSafeLink(id, "any_token")
        );

        assertTrue(ex.getMessage().contains("scaduto"));
        // Il sistema deve pulire il DB eliminando l'entry scaduta
        verify(safeSendRepository).delete(entry);
        verify(vaultService).notifyObservers();
    }

    @Test
    @DisplayName("Accesso link: Fallimento se il token è errato (Nessuna cancellazione)")
    void accessSafeLink_WrongToken() {
        UUID id = UUID.randomUUID();
        String correctHash = "correct_hash";
        SafeSendEntryEntity entry = createEntry(id, new byte[0], correctHash, 1);

        when(safeSendRepository.findById(id)).thenReturn(Optional.of(entry));
        when(passwordHasher.verify("wrong_token", correctHash)).thenReturn(false);

        assertThrows(InvalidTokenException.class, () ->
                service.accessSafeLink(id, "wrong_token")
        );

        // Se il token è solo sbagliato, non cancelliamo (permettiamo riprova o evitiamo DoS)
        verify(safeSendRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Accesso link: Fallimento se l'ID non esiste nel database")
    void accessSafeLink_NotFound() {
        UUID id = UUID.randomUUID();
        when(safeSendRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () ->
                service.accessSafeLink(id, "token")
        );
    }

    // --- HELPER ---

    private SafeSendEntryEntity createEntry(UUID id, byte[] data, String hash, int hoursFromNow) {
        SafeSendEntryEntity e = new SafeSendEntryEntity();
        e.setId(id);
        e.setEncryptedContent(data);
        e.setTokenHash(hash);
        e.setExpiresAt(LocalDateTime.now().plusHours(hoursFromNow));
        e.setOneTime(true);
        return e;
    }
}