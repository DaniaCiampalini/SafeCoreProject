package com.safecore.persistence.repository;

import com.safecore.persistence.entity.SafeSendEntry;
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

    private SafeSendEntry activeEntry;
    private SafeSendEntry expiredEntry;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        // Entry attiva (non scaduta)
        activeEntry = new SafeSendEntry();
        activeEntry.setEncryptedContent("encrypted-data".getBytes());
        activeEntry.setExpiresAt(now.plusHours(1));
        activeEntry.setOneTime(true);
        activeEntry.setCreator(user);
        activeEntry = entityManager.persistAndFlush(activeEntry);

        // Entry scaduta
        expiredEntry = new SafeSendEntry();
        expiredEntry.setEncryptedContent("expired-data".getBytes());
        expiredEntry.setExpiresAt(now.minusHours(1));
        expiredEntry.setOneTime(true);
        expiredEntry.setCreator(user);
        expiredEntry = entityManager.persistAndFlush(expiredEntry);
    }

    @Test
    void save_andFindById() {
        UserEntity user = new UserEntity();
        user.setEmail("saveandfind@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntry newEntry = new SafeSendEntry();
        newEntry.setEncryptedContent("new-data".getBytes());
        newEntry.setExpiresAt(LocalDateTime.now().plusDays(1));
        newEntry.setOneTime(true);
        newEntry.setCreator(user);

        SafeSendEntry saved = safeSendRepository.save(newEntry);
        entityManager.flush();
        entityManager.clear();

        var found = safeSendRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertArrayEquals("new-data".getBytes(), found.get().getEncryptedContent());
    }

    @Test
    void findAll_returnsAllEntries() {
        UserEntity user = new UserEntity();
        user.setEmail("findall@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntry entry3 = new SafeSendEntry();
        entry3.setEncryptedContent("data-3".getBytes());
        entry3.setExpiresAt(LocalDateTime.now().plusHours(2));
        entry3.setOneTime(true);
        entry3.setCreator(user);
        entityManager.persistAndFlush(entry3);

        List<SafeSendEntry> entries = safeSendRepository.findAll();

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
        safeSendRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        entityManager.flush();
        entityManager.clear();

        List<SafeSendEntry> remaining = safeSendRepository.findAll();

        assertEquals(1, remaining.size());
        assertArrayEquals("encrypted-data".getBytes(), remaining.get(0).getEncryptedContent());
    }

    @Test
    void deleteByExpiresAtBefore_whenNoExpiredEntries_deletesNothing() {
        // Prima rimuoviamo le entry scadute
        safeSendRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        entityManager.flush();

        long countBefore = safeSendRepository.count();

        // Ora proviamo di nuovo - non dovrebbe rimuovere nulla
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
        user.setEmail("nullcontent@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntry entry = new SafeSendEntry();
        entry.setEncryptedContent(null);
        entry.setExpiresAt(LocalDateTime.now().plusHours(1));
        entry.setOneTime(true);
        entry.setCreator(user);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(entry);
        });
    }

    @Test
    void save_withNullExpiresAt_throwsException() {
        UserEntity user = new UserEntity();
        user.setEmail("nullexpires@example.com");
        user.setPasswordHash("hash");
        user.setMfaEnabled(false);
        user = entityManager.persistAndFlush(user);

        SafeSendEntry entry = new SafeSendEntry();
        entry.setEncryptedContent("data".getBytes());
        entry.setExpiresAt(null);
        entry.setOneTime(true);
        entry.setCreator(user);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(entry);
        });
    }

    @Test
    void updateEntry_modifiesFields() {
        activeEntry.setEncryptedContent("updated-data".getBytes());

        SafeSendEntry updated = safeSendRepository.save(activeEntry);
        entityManager.flush();
        entityManager.clear();

        var found = safeSendRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        assertArrayEquals("updated-data".getBytes(), found.get().getEncryptedContent());
    }
}
