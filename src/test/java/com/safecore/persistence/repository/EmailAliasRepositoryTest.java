package com.safecore.persistence.repository;

import com.safecore.persistence.entity.EmailAliasEntity;
import com.safecore.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per EmailAliasRepository.
 * Verifica il corretto funzionamento delle operazioni CRUD e query per alias email.
 */
@DataJpaTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EmailAliasRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmailAliasRepository emailAliasRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setEmail("user@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setMfaEnabled(false);
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void save_andFindById() {
        EmailAliasEntity alias = new EmailAliasEntity();
        alias.setAliasEmail("alias1@example.com");
        alias.setServiceName("service1");
        alias.setUser(testUser);

        EmailAliasEntity saved = emailAliasRepository.save(alias);
        entityManager.flush();
        entityManager.clear();

        var found = emailAliasRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("alias1@example.com", found.get().getAliasEmail());
        assertEquals(testUser.getId(), found.get().getUser().getId());
    }

    @Test
    void findByUserEmail_whenAliasesExist_returnsList() {
        EmailAliasEntity alias1 = new EmailAliasEntity();
        alias1.setAliasEmail("alias1@example.com");
        alias1.setServiceName("service1");
        alias1.setUser(testUser);
        entityManager.persistAndFlush(alias1);

        EmailAliasEntity alias2 = new EmailAliasEntity();
        alias2.setAliasEmail("alias2@example.com");
        alias2.setServiceName("service2");
        alias2.setUser(testUser);
        entityManager.persistAndFlush(alias2);

        List<EmailAliasEntity> aliases = emailAliasRepository.findByUserEmail("user@example.com");

        assertEquals(2, aliases.size());
        assertTrue(aliases.stream().anyMatch(a -> "alias1@example.com".equals(a.getAliasEmail())));
        assertTrue(aliases.stream().anyMatch(a -> "alias2@example.com".equals(a.getAliasEmail())));
    }

    @Test
    void findByUserEmail_whenNoAliases_returnsEmptyList() {
        List<EmailAliasEntity> aliases = emailAliasRepository.findByUserEmail("user@example.com");

        assertTrue(aliases.isEmpty());
    }

    @Test
    void findByUserEmail_withNonExistentUser_returnsEmptyList() {
        List<EmailAliasEntity> aliases = emailAliasRepository.findByUserEmail("nonexistent@example.com");

        assertTrue(aliases.isEmpty());
    }

    @Test
    void findByUserEmail_withNullEmail_returnsEmptyList() {
        List<EmailAliasEntity> aliases = emailAliasRepository.findByUserEmail(null);

        assertTrue(aliases.isEmpty());
    }

    @Test
    void findAll_returnsAllAliases() {
        EmailAliasEntity alias1 = new EmailAliasEntity();
        alias1.setAliasEmail("alias1@example.com");
        alias1.setServiceName("service1");
        alias1.setUser(testUser);
        entityManager.persistAndFlush(alias1);

        // Crea un altro utente con i suoi alias
        UserEntity user2 = new UserEntity();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash2");
        user2 = entityManager.persistAndFlush(user2);

        EmailAliasEntity alias2 = new EmailAliasEntity();
        alias2.setAliasEmail("alias2@example.com");
        alias2.setServiceName("service2");
        alias2.setUser(user2);
        entityManager.persistAndFlush(alias2);

        List<EmailAliasEntity> allAliases = emailAliasRepository.findAll();

        assertEquals(2, allAliases.size());
    }

    @Test
    void deleteById_removesAlias() {
        EmailAliasEntity alias = new EmailAliasEntity();
        alias.setAliasEmail("alias1@example.com");
        alias.setServiceName("service1");
        alias.setUser(testUser);
        EmailAliasEntity saved = entityManager.persistAndFlush(alias);

        emailAliasRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var found = emailAliasRepository.findById(saved.getId());

        assertFalse(found.isPresent());
    }

    @Test
    void count_returnsCorrectCount() {
        EmailAliasEntity alias1 = new EmailAliasEntity();
        alias1.setAliasEmail("alias1@example.com");
        alias1.setServiceName("service1");
        alias1.setUser(testUser);
        entityManager.persistAndFlush(alias1);

        EmailAliasEntity alias2 = new EmailAliasEntity();
        alias2.setAliasEmail("alias2@example.com");
        alias2.setServiceName("service2");
        alias2.setUser(testUser);
        entityManager.persistAndFlush(alias2);

        long count = emailAliasRepository.count();

        assertEquals(2, count);
    }

    @Test
    void save_withNullAliasEmail_throwsException() {
        EmailAliasEntity alias = new EmailAliasEntity();
        alias.setAliasEmail(null);
        alias.setServiceName("service1");
        alias.setUser(testUser);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(alias);
        });
    }

    @Test
    void save_withNullUser_throwsException() {
        EmailAliasEntity alias = new EmailAliasEntity();
        alias.setAliasEmail("alias@example.com");
        alias.setServiceName("service1");
        alias.setUser(null);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(alias);
        });
    }

    @Test
    void updateAlias_modifiesFields() {
        EmailAliasEntity alias = new EmailAliasEntity();
        alias.setAliasEmail("old-alias@example.com");
        alias.setServiceName("service1");
        alias.setUser(testUser);
        EmailAliasEntity saved = entityManager.persistAndFlush(alias);

        saved.setAliasEmail("new-alias@example.com");

        EmailAliasEntity updated = emailAliasRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        var found = emailAliasRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        assertEquals("new-alias@example.com", found.get().getAliasEmail());
    }

    @Test
    void deleteByUserEmail_removesAllAliasesForUser() {
        EmailAliasEntity alias1 = new EmailAliasEntity();
        alias1.setAliasEmail("alias1@example.com");
        alias1.setServiceName("service1");
        alias1.setUser(testUser);
        entityManager.persistAndFlush(alias1);

        EmailAliasEntity alias2 = new EmailAliasEntity();
        alias2.setAliasEmail("alias2@example.com");
        alias2.setServiceName("service2");
        alias2.setUser(testUser);
        entityManager.persistAndFlush(alias2);

        // Verifica che ci siano 2 alias
        List<EmailAliasEntity> beforeDelete = emailAliasRepository.findByUserEmail("user@example.com");
        assertEquals(2, beforeDelete.size());

        // Elimina tutti gli alias dell'utente
        emailAliasRepository.deleteAll(beforeDelete);
        entityManager.flush();

        // Verifica che non ci siano più alias
        List<EmailAliasEntity> afterDelete = emailAliasRepository.findByUserEmail("user@example.com");
        assertTrue(afterDelete.isEmpty());
    }
}
