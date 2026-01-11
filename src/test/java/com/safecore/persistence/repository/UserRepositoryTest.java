package com.safecore.persistence.repository;

import com.safecore.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per UserRepository.
 * Verifica il corretto funzionamento delle operazioni CRUD e query personalizzate.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword123");
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void findByEmail_whenUserExists_returnsUser() {
        Optional<UserEntity> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
        assertEquals("hashedPassword123", found.get().getPasswordHash());
    }

    @Test
    void findByEmail_whenUserNotExists_returnsEmpty() {
        Optional<UserEntity> found = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmail_withNullEmail_returnsEmpty() {
        Optional<UserEntity> found = userRepository.findByEmail(null);

        assertFalse(found.isPresent());
    }

    @Test
    void existsByEmail_whenUserExists_returnsTrue() {
        boolean exists = userRepository.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_whenUserNotExists_returnsFalse() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void existsByEmail_withNullEmail_returnsFalse() {
        boolean exists = userRepository.existsByEmail(null);

        assertFalse(exists);
    }

    @Test
    void updatePassword_whenUserExists_updatesPassword() {
        String newPassword = "newHashedPassword456";
        userRepository.updatePassword("test@example.com", newPassword);
        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> updated = userRepository.findByEmail("test@example.com");

        assertTrue(updated.isPresent());
        assertEquals(newPassword, updated.get().getPasswordHash());
    }

    @Test
    void updatePassword_whenUserNotExists_doesNothing() {
        // Non dovrebbe lanciare eccezione, semplicemente non aggiorna nulla
        assertDoesNotThrow(() -> {
            userRepository.updatePassword("nonexistent@example.com", "newPassword");
        });
    }

    @Test
    void save_andFindById() {
        UserEntity newUser = new UserEntity();
        newUser.setEmail("newuser@example.com");
        newUser.setPasswordHash("passwordHash");

        UserEntity saved = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> found = userRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("newuser@example.com", found.get().getEmail());
    }

    @Test
    void deleteById_removesUser() {
        UUID userId = testUser.getId();

        userRepository.deleteById(userId);
        entityManager.flush();
        entityManager.clear();

        Optional<UserEntity> found = userRepository.findById(userId);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_returnsAllUsers() {
        UserEntity user2 = new UserEntity();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash2");
        entityManager.persistAndFlush(user2);

        UserEntity user3 = new UserEntity();
        user3.setEmail("user3@example.com");
        user3.setPasswordHash("hash3");
        entityManager.persistAndFlush(user3);

        var users = userRepository.findAll();

        assertEquals(3, users.size());
    }

    @Test
    void count_returnsCorrectCount() {
        UserEntity user2 = new UserEntity();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hash2");
        entityManager.persistAndFlush(user2);

        long count = userRepository.count();

        assertEquals(2, count);
    }
}
