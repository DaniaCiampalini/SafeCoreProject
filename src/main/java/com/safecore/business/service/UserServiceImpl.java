package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.persistence.dao.UserDao;
import com.safecore.security.PasswordHasher;

import java.util.Optional;

/**
 * Implementazione concreta del Service.
 *
 * Responsabilità:
 * - Validazione input
 * - Hashing password
 * - Coordinamento DAO
 *
 * NON fa:
 * - Query SQL
 * - Gestione EntityManager
 */
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    /**
     * Dependency Injection manuale.
     *
     * Scelta SE:
     * - Facilita test
     * - Riduce accoppiamento
     */
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User register(String email, String plainPassword) {

        // Regola di business: email unica
        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }

        // Hashing sicuro della password
        String hashedPassword = PasswordHasher.hash(plainPassword);

        // Costruzione Domain Model
        User user = new UserBuilder()
                .email(email)
                .passwordHash(hashedPassword)
                .mfaEnabled(false) // default
                .build();

        // Persistenza
        userDao.save(user);

        return user;
    }

    @Override
    public Optional<User> login(String email, String plainPassword) {

        Optional<User> userOpt = userDao.findByEmail(email);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // Verifica password
        if (!PasswordHasher.verify(plainPassword, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(user);
    }
}
