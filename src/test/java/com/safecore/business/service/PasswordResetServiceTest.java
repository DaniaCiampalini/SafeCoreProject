package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserFactory;
import com.safecore.persistence.dao.PasswordResetTokenDao;
import com.safecore.persistence.dao.UserDao;
import com.safecore.persistence.entity.PasswordResetTokenEntity;
import com.safecore.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PasswordResetServiceTest {

    private final FakeUserDao userDao = new FakeUserDao();
    private final FakeTokenDao tokenDao = new FakeTokenDao();
    private final PasswordResetService service =
            new PasswordResetServiceImpl(userDao, tokenDao);

    @Test
    void resetPassword_success() {
        userDao.addUser("a@mail.com", "OldPass123!");

        String token = service.requestReset("a@mail.com");
        assertNotNull(token);

        assertDoesNotThrow(() ->
                service.resetPassword("a@mail.com", token, "NewPass123!"));
    }

    @Test
    void resetPassword_invalidToken_fails() {
        userDao.addUser("x@mail.com", "Pass123!");

        assertThrows(IllegalArgumentException.class, () ->
                service.resetPassword("x@mail.com", "badtoken", "NewPass123!"));
    }

    // ---------- FAKE USER DAO ----------
    static class FakeUserDao implements UserDao {

        private final Map<String, User> db = new HashMap<>();

        void addUser(String email, String plainPassword) {
            db.put(email, UserFactory.createNew(
                    1L,
                    email,
                    PasswordHasher.hash(plainPassword),
                    false
            ));
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(db.get(email));
        }

        @Override
        public boolean existsByEmail(String email) {
            return db.containsKey(email);
        }

        @Override
        public void save(User user) {
            db.put(user.getEmail(), user);
        }

        @Override
        public void updatePassword(String email, String newPasswordHash) {
            User old = db.get(email);
            db.put(email, UserFactory.createNew(
                    old.getId(),
                    old.getEmail(),
                    newPasswordHash,
                    old.isMfaEnabled()
            ));
        }
    }

    // ---------- FAKE TOKEN DAO ----------
    static class FakeTokenDao implements PasswordResetTokenDao {

        private final Map<String, PasswordResetTokenEntity> tokens = new HashMap<>();

        @Override
        public void save(PasswordResetTokenEntity token) {
            tokens.put(token.getEmail(), token);
        }

        @Override
        public PasswordResetTokenEntity findValidTokenByEmail(String email) {
            PasswordResetTokenEntity t = tokens.get(email);
            if (t == null || t.isUsed() ||
                    t.getExpiresAt().isBefore(LocalDateTime.now())) {
                return null;
            }
            return t;
        }

        @Override
        public void update(PasswordResetTokenEntity token) {
            tokens.put(token.getEmail(), token);
        }
    }
}
