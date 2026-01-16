package com.safecore.persistence.repository;

import com.safecore.persistence.entity.SafeSendEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per SafeSendRepository.
 * Verifica il corretto funzionamento delle operazioni CRUD e pulizia delle entry scadute.
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SafeSendRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SafeSendRepository safeSendRepository;

    private SafeSendEntryEntity activeEntry;
    private SafeSendEntryEntity expiredEntry;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Creazione utente base per il setup
        UserEntity user = new UserEntity();
        user.setEmail("main-test@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        // Entry attiva (non scaduta)
        activeEntry = new SafeSendEntryEntity();
        activeEntry.setEncryptedContent("encrypted-data".getBytes());
        activeEntry.setExpiresAt(now.plusHours(1));
        activeEntry.setOneTime(true);
        activeEntry.setTokenHash("token-attivo-" + UUID.randomUUID()); // CORRETTO: Campo obbligatorio
        activeEntry.setUser(user);
        activeEntry = entityManager.persistAndFlush(activeEntry);

        // Entry scaduta
        expiredEntry = new SafeSendEntryEntity();
        expiredEntry.setEncryptedContent("expired-data".getBytes());
        expiredEntry.setExpiresAt(now.minusHours(1));
        expiredEntry.setOneTime(true);
        expiredEntry.setTokenHash("token-scaduto-" + UUID.randomUUID()); // CORRETTO: Campo obbligatorio e unico
        expiredEntry.setUser(user);
        expiredEntry = entityManager.persistAndFlush(expiredEntry);
    }

    @Test
    void save_andFindById() {
        UserEntity user = new UserEntity();
        user.setEmail("saveandfind-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntryEntity newEntry = new SafeSendEntryEntity();
        newEntry.setEncryptedContent("new-data".getBytes());
        newEntry.setExpiresAt(LocalDateTime.now().plusDays(1));
        newEntry.setOneTime(true);
        newEntry.setTokenHash("unique-token-" + UUID.randomUUID()); // CORRETTO
        newEntry.setUser(user);

        SafeSendEntryEntity saved = safeSendRepository.save(newEntry);
        entityManager.flush();
        entityManager.clear();

        var found = safeSendRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertArrayEquals("new-data".getBytes(), found.get().getEncryptedContent());
    }

    @Test
    void findAll_returnsAllEntries() {
        UserEntity user = new UserEntity();
        user.setEmail("findall-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntryEntity entry3 = new SafeSendEntryEntity();
        entry3.setEncryptedContent("data-3".getBytes());
        entry3.setExpiresAt(LocalDateTime.now().plusHours(2));
        entry3.setOneTime(true);
        entry3.setTokenHash("token-3-" + UUID.randomUUID()); // CORRETTO
        entry3.setUser(user);
        entityManager.persistAndFlush(entry3);

        List<SafeSendEntryEntity> entries = safeSendRepository.findAll();

        // 2 del setup + 1 nuova = 3
        assertEquals(3, entries.size());
    }

    @Test
    void deleteById_removesEntry() {
        UUID entryId = activeEntry.getId();

        safeSendRepository.deleteById(entryId);
        entityManager.flush();
        entityManager.clear();

        var found = safeSendRepository.findById(entryId);

        assertFalse(found.isPresent());
    }

    @Test
    void deleteByExpiresAtBefore_deletesOnlyExpiredEntries() {
        // Esegue la cancellazione di quelle precedenti a 'now'
        safeSendRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        List<SafeSendEntryEntity> remaining = safeSendRepository.findAll();

        // Dovrebbe restare solo activeEntry
        assertEquals(1, remaining.size());
        assertArrayEquals("encrypted-data".getBytes(), remaining.get(0).getEncryptedContent());
    }

    @Test
    void deleteByExpiresAtBefore_whenNoExpiredEntries_deletesNothing() {
        // Prima rimuoviamo le entry scadute per pulire il campo
        safeSendRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        entityManager.flush();

        long countBefore = safeSendRepository.count();

        // Ora proviamo di nuovo con la stessa data - non dovrebbe esserci nulla da rimuovere
        safeSendRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        entityManager.flush();

        long countAfter = safeSendRepository.count();

        assertEquals(countBefore, countAfter);
    }

    @Test
    void count_returnsCorrectCount() {
        long count = safeSendRepository.count();
        assertEquals(2, count);
    }

    @Test
    void save_withNullEncryptedContent_throwsException() {
        UserEntity user = new UserEntity();
        user.setEmail("nullcontent-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user = entityManager.persistAndFlush(user);

        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setEncryptedContent(null); // Questo deve scatenare l'errore
        entry.setTokenHash("token-null-content");
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setOneTime(true);
        entry.setUser(user);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(entry);
        });
    }

    @Test
    void save_withNullExpiresAt_throwsException() {
        UserEntity user = new UserEntity();
        user.setEmail("nullexpires-" + UUID.randomUUID() + "@example.com");
        user.setPasswordHash("hash");
        user = entityManager.persistAndFlush(user);

        SafeSendEntryEntity entry = new SafeSendEntryEntity();
        entry.setEncryptedContent("data".getBytes());
        entry.setExpiresAt(null); // Questo deve scatenare l'errore
        entry.setTokenHash("token-null-expiry");
        entry.setOneTime(true);
        entry.setUser(user);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(entry);
        });
    }

    @Test
    void updateEntry_modifiesFields() {
        activeEntry.setEncryptedContent("updated-data".getBytes());

        SafeSendEntryEntity updated = safeSendRepository.save(activeEntry);
        entityManager.flush();
        entityManager.clear();

        var found = safeSendRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        assertArrayEquals("updated-data".getBytes(), found.get().getEncryptedContent());
    }
}