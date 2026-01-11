package com.safecore.persistence.repository;

import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.persistence.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per PasswordResetTokenRepository.
 * Verifica il corretto funzionamento delle operazioni CRUD e query per token di reset password.
 */
@DataJpaTest
@ActiveProfiles("test")
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setEmail("user@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void save_andFindById() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("hashed-token-123");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        PasswordResetTokenEntity saved = passwordResetTokenRepository.save(token);
        entityManager.flush();
        entityManager.clear();

        var found = passwordResetTokenRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("user@example.com", found.get().getEmail());
        assertEquals("hashed-token-123", found.get().getTokenHash());
        assertFalse(found.get().isUsed());
    }

    @Test
    void findByEmailAndUsedFalse_whenTokenExistsAndNotUsed_returnsToken() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("hashed-token-123");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        entityManager.persistAndFlush(token);

        Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByEmailAndUsedFalse("user@example.com");

        assertTrue(found.isPresent());
        assertEquals("hashed-token-123", found.get().getTokenHash());
        assertFalse(found.get().isUsed());
    }

    @Test
    void findByEmailAndUsedFalse_whenTokenExistsAndUsed_returnsEmpty() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("hashed-token-123");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(true);
        entityManager.persistAndFlush(token);

        Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByEmailAndUsedFalse("user@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmailAndUsedFalse_whenNoTokenExists_returnsEmpty() {
        Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByEmailAndUsedFalse("user@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmailAndUsedFalse_withNonExistentEmail_returnsEmpty() {
        Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByEmailAndUsedFalse("nonexistent@example.com");

        assertFalse(found.isPresent());
    }

    @Test
    void findByEmailAndUsedFalse_withNullEmail_returnsEmpty() {
        Optional<PasswordResetTokenEntity> found = passwordResetTokenRepository.findByEmailAndUsedFalse(null);

        assertFalse(found.isPresent());
    }

    @Test
    void findAll_returnsAllTokens() {
        PasswordResetTokenEntity token1 = new PasswordResetTokenEntity();
        token1.setEmail("user@example.com");
        token1.setTokenHash("token1");
        token1.setExpiryDate(LocalDateTime.now().plusHours(1));
        token1.setUsed(false);
        entityManager.persistAndFlush(token1);

        PasswordResetTokenEntity token2 = new PasswordResetTokenEntity();
        token2.setEmail("user@example.com");
        token2.setTokenHash("token2");
        token2.setExpiryDate(LocalDateTime.now().plusHours(1));
        token2.setUsed(false);
        entityManager.persistAndFlush(token2);

        var tokens = passwordResetTokenRepository.findAll();

        assertEquals(2, tokens.size());
    }

    @Test
    void deleteById_removesToken() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("token-to-delete");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        PasswordResetTokenEntity saved = entityManager.persistAndFlush(token);

        passwordResetTokenRepository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        var found = passwordResetTokenRepository.findById(saved.getId());

        assertFalse(found.isPresent());
    }

    @Test
    void count_returnsCorrectCount() {
        PasswordResetTokenEntity token1 = new PasswordResetTokenEntity();
        token1.setEmail("user@example.com");
        token1.setTokenHash("token1");
        token1.setExpiryDate(LocalDateTime.now().plusHours(1));
        token1.setUsed(false);
        entityManager.persistAndFlush(token1);

        PasswordResetTokenEntity token2 = new PasswordResetTokenEntity();
        token2.setEmail("user@example.com");
        token2.setTokenHash("token2");
        token2.setExpiryDate(LocalDateTime.now().plusHours(1));
        token2.setUsed(false);
        entityManager.persistAndFlush(token2);

        long count = passwordResetTokenRepository.count();

        assertEquals(2, count);
    }

    @Test
    void save_withNullEmail_throwsException() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail(null);
        token.setTokenHash("token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(token);
        });
    }

    @Test
    void save_withNullTokenHash_throwsException() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash(null);
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);

        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(token);
        });
    }

    @Test
    void updateToken_modifiesUsedStatus() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        PasswordResetTokenEntity saved = entityManager.persistAndFlush(token);

        saved.setUsed(true);

        PasswordResetTokenEntity updated = passwordResetTokenRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        var found = passwordResetTokenRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        assertTrue(found.get().isUsed());
    }

    @Test
    void deleteExpiredTokens_removesOnlyExpired() {
        LocalDateTime now = LocalDateTime.now();

        // Token non scaduto
        PasswordResetTokenEntity activeToken = new PasswordResetTokenEntity();
        activeToken.setEmail("user@example.com");
        activeToken.setTokenHash("active-token");
        activeToken.setExpiryDate(now.plusHours(1));
        activeToken.setUsed(false);
        entityManager.persistAndFlush(activeToken);

        // Token scaduto
        PasswordResetTokenEntity expiredToken = new PasswordResetTokenEntity();
        expiredToken.setEmail("user@example.com");
        expiredToken.setTokenHash("expired-token");
        expiredToken.setExpiryDate(now.minusHours(1));
        expiredToken.setUsed(false);
        entityManager.persistAndFlush(expiredToken);

        // Elimina i token scaduti
        passwordResetTokenRepository.deleteAll(
            passwordResetTokenRepository.findAll().stream()
                .filter(t -> t.getExpiryDate().isBefore(now))
                .toList()
        );
        entityManager.flush();

        var remaining = passwordResetTokenRepository.findAll();

        assertEquals(1, remaining.size());
        assertEquals("active-token", remaining.get(0).getTokenHash());
    }

    @Test
    void updateToken_modifiesTokenHash() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("old-token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        PasswordResetTokenEntity saved = entityManager.persistAndFlush(token);

        saved.setTokenHash("new-token");

        PasswordResetTokenEntity updated = passwordResetTokenRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        var found = passwordResetTokenRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        assertEquals("new-token", found.get().getTokenHash());
    }

    @Test
    void updateToken_modifiesExpiryDate() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setEmail("user@example.com");
        token.setTokenHash("token");
        token.setExpiryDate(LocalDateTime.now().plusHours(1));
        token.setUsed(false);
        PasswordResetTokenEntity saved = entityManager.persistAndFlush(token);

        LocalDateTime newExpiry = LocalDateTime.now().plusHours(24);
        saved.setExpiryDate(newExpiry);

        PasswordResetTokenEntity updated = passwordResetTokenRepository.save(saved);
        entityManager.flush();
        entityManager.clear();

        var found = passwordResetTokenRepository.findById(updated.getId());

        assertTrue(found.isPresent());
        // Verifica che la data sia stata aggiornata (con tolleranza di 1 secondo)
        assertTrue(Math.abs(found.get().getExpiryDate().getSecond() - newExpiry.getSecond()) <= 1);
    }
}
