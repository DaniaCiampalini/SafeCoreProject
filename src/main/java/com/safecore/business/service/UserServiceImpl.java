package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public User register(String email, String plainPassword) {
        validatePasswordStrength(plainPassword);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email già registrata");
        }

        String hashedPassword = PasswordHasher.hash(plainPassword);

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
                .filter(u -> PasswordHasher.verify(plainPassword, u.getPasswordHash()))
                .map(u -> new UserBuilder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .passwordHash(u.getPasswordHash())
                        .build());
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8 ||
                PasswordStrengthEvaluator.evaluate(password) == PasswordStrengthEvaluator.Strength.WEAK) {
            throw new IllegalArgumentException("Password non valida o troppo debole");
        }
    }
}