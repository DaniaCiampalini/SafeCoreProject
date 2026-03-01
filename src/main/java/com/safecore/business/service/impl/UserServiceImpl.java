package com.safecore.business.service.impl;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserAlreadyExistsException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.business.exception.WeakPasswordException;
import com.safecore.business.service.UserService;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.KeyManager;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
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
    private final KeyManager keyManager;
    private final PasswordEntryRepository passwordEntryRepository;

    // Costruttore con iniezione delle dipendenze
    public UserServiceImpl(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           PasswordStrengthEvaluator strengthEvaluator,
                           KeyManager keyManager,
                           PasswordEntryRepository passwordEntryRepository) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.strengthEvaluator = strengthEvaluator;
        this.keyManager = keyManager;
        this.passwordEntryRepository = passwordEntryRepository;
    }

    @Override
    @Transactional
    public User register(String email, String plainPassword) {
        validatePasswordStrength(plainPassword);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        String hashedPassword = passwordHasher.hash(plainPassword);

        // Creazione del salt unico per l'utente durante la registrazione
        byte[] salt = new byte[32];
        new SecureRandom().nextBytes(salt);

        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setPasswordHash(hashedPassword);
        entity.setDerivationSalt(salt);

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
                .map(u -> {
                    keyManager.initialize(plainPassword, u.getDerivationSalt());
                    return new UserBuilder()
                            .id(u.getId())
                            .email(u.getEmail())
                            .passwordHash(u.getPasswordHash())
                            .build();
                });
    }

    @Override
    public void logout() { keyManager.clear(); }

    /**
     * Elimina in modo sicuro l'account dell'utente.
     * Prima verifica la password, poi sovrascrive i dati sensibili (sanitizzazione),
     * infine elimina fisicamente l'utente e tutte le sue entry.
     */
    @Override
    @Transactional
    public void secureDeleteAccount(String email, String plainPassword) {
        // 1. Trova l'utente
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utente non trovato: " + email));

        // 2. Verifica la password (riautenticazione)
        if (!passwordHasher.verify(plainPassword, user.getPasswordHash())) {
            throw new InvalidTokenException("Password errata. Impossibile eliminare l'account.");
        }

        // 3. Recupera tutte le entry dell'utente
        List<PasswordEntryEntity> userEntries = passwordEntryRepository.findByUserEmail(email);

        // 4. SANITIZZAZIONE: Sovrascrive i dati sensibili con byte casuali
        SecureRandom secureRandom = new SecureRandom();
        for (PasswordEntryEntity entry : userEntries) {
            byte[] randomBytes = new byte[entry.getEncryptedPassword().length];
            secureRandom.nextBytes(randomBytes);
            entry.setEncryptedPassword(randomBytes);
            entry.setServiceName("DELETED");
            entry.setUsername("DELETED");
        }

        // 5. Forza la persistenza della sovrascrittura nel database
        passwordEntryRepository.saveAll(userEntries);
        passwordEntryRepository.flush();

        // 6. Elimina fisicamente tutte le entry
        passwordEntryRepository.deleteAll(userEntries);
        passwordEntryRepository.flush();

        // 7. Sovrascrive i dati sensibili dell'utente
        byte[] randomSalt = new byte[user.getDerivationSalt().length];
        secureRandom.nextBytes(randomSalt);
        user.setDerivationSalt(randomSalt);
        user.setPasswordHash("DELETED_" + secureRandom.nextLong());
        userRepository.save(user);
        userRepository.flush();

        // 8. Elimina fisicamente l'utente
        userRepository.delete(user);
        userRepository.flush();

        // 9. Pulisce la chiave di cifratura dalla memoria
        keyManager.clear();
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8 ||
                strengthEvaluator.evaluate(password) == PasswordStrengthEvaluator.Strength.WEAK) {
            throw new WeakPasswordException("La password fornita non soddisfa i requisiti minimi di sicurezza.");
        }
    }
}
