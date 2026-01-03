package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserFactory;
import com.safecore.persistence.dao.UserDao;
import com.safecore.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final UserDao fakeDao = new InMemoryUserDao();
    private final UserService service = new UserServiceImpl(fakeDao);

    @Test
    void registerAndLogin_success() {
        service.register("test@mail.com", "Password123!");
        assertDoesNotThrow(() ->
                service.login("test@mail.com", "Password123!"));
    }

    @Test
    void register_duplicateEmail_fails() {
        service.register("a@mail.com", "Password123!");
        assertThrows(IllegalArgumentException.class,
                () -> service.register("a@mail.com", "Password123!"));
    }

    @Test
    void login_wrongPassword_fails() {
        service.register("x@mail.com", "Password123!");
        assertThrows(IllegalArgumentException.class,
                () -> service.login("x@mail.com", "WrongPass"));
    }

    // ---------- FAKE DAO ----------
    static class InMemoryUserDao implements UserDao {

        private final Map<String, User> db = new HashMap<>();
        private long idSequence = 1;

        @Override
        public void save(User user) {
            db.put(user.getEmail(), user);
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
        public void updatePassword(String email, String newPasswordHash) {
            User old = db.get(email);
            if (old == null) {
                throw new IllegalArgumentException("User not found");
            }

            User updated = UserFactory.createNew(
                    old.getId(),
                    old.getEmail(),
                    newPasswordHash,
                    old.isMfaEnabled()
            );

            db.put(email, updated);
        }
    }
}
