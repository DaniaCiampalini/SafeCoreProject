package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.business.exception.UserAlreadyExistsException; // Da creare
import com.safecore.business.exception.WeakPasswordException;       // Da creare
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
    private final PasswordHasher passwordHasher; 
    private final PasswordStrengthEvaluator strengthEvaluator; 

    // Qui le dipendenze vengono iniettate da Spring in automatico.
    // Usiamo le interfacce (es. PasswordHasher) così se cambiamo implementazione
    // (magari passiamo da BCrypt ad Argon2) questo codice non cambia.
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
        // Prima di tutto, controlliamo se la password è abbastanza robusta.
        // Se non lo è, lanciamo un'eccezione e il database non viene toccato.
        validatePasswordStrength(plainPassword);

        // Vediamo se l'utente esiste già
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        // MAI salvare la password in chiaro! Creiamo un hash sicuro.
        String hashedPassword = passwordHasher.hash(plainPassword);

        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setPasswordHash(hashedPassword);
        entity.setMfaEnabled(false);

        // Salviamo l'Entity sul DB
        UserEntity saved = userRepository.save(entity);

        // Restituiamo l'oggetto di dominio "User" (creato con il Builder pattern)
        // così chi chiama il service non deve preoccuparsi dei dettagli del database.
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