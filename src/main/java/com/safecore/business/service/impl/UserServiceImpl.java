package com.safecore.business.service.impl;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.business.exception.UserAlreadyExistsException;
import com.safecore.business.exception.WeakPasswordException;
import com.safecore.business.service.UserService;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementazione del servizio per la gestione degli utenti.
 * Qui gestiamo la registrazione e il login.
 * Usiamo transazioni per garantire la coerenza dei dati.
 */

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final PasswordStrengthEvaluator strengthEvaluator;

    // Costruttore con iniezione delle dipendenze
    public UserServiceImpl(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           PasswordStrengthEvaluator strengthEvaluator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.strengthEvaluator = strengthEvaluator;
    }

    @Override
    @Transactional
    public User register(String email, String plainPassword) {
        validatePasswordStrength(plainPassword);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        String hashedPassword = passwordHasher.hash(plainPassword);

        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setPasswordHash(hashedPassword);
        entity.setMfaEnabled(false);

        UserEntity saved = userRepository.save(entity);

        return new UserBuilder()
                .id(saved.getId())
                .email(saved.getEmail())
                .passwordHash(saved.getPasswordHash())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> login(String email, String plainPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordHasher.verify(plainPassword, u.getPasswordHash()))
                .map(u -> new UserBuilder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .passwordHash(u.getPasswordHash())
                        .build());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8 ||
                strengthEvaluator.evaluate(password) == PasswordStrengthEvaluator.Strength.WEAK) {
            throw new WeakPasswordException("La password fornita non soddisfa i requisiti minimi di sicurezza.");
        }
    }
}