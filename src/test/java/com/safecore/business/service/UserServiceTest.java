package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.service.impl.UserServiceImpl;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.KeyManager;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private PasswordStrengthEvaluator strengthEvaluator;
    private KeyManager keyManager;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        strengthEvaluator = mock(PasswordStrengthEvaluator.class);
        keyManager = mock(KeyManager.class);
        service = new UserServiceImpl(userRepository, passwordHasher, strengthEvaluator, keyManager);
    }

    @Test
    void registerAndLogin_success() {
        String email = "test@mail.com";
        String pass = "Password123!";
        byte[] salt =  new byte[32];

        when(strengthEvaluator.evaluate(pass)).thenReturn(PasswordStrengthEvaluator.Strength.STRONG);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordHasher.hash(pass)).thenReturn("hashed_pass");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail(email);
        savedEntity.setPasswordHash("hashed_pass");
        savedEntity.setDerivationSalt(salt);

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedEntity));
        when(passwordHasher.verify(pass, "hashed_pass")).thenReturn(true);

        User registered = service.register(email, pass);
        assertNotNull(registered);

        Optional<User> loggedUser = service.login(email, pass);
        assertTrue(loggedUser.isPresent());

        verify(keyManager).initialize(pass, salt);
    }

    @Test
    void logout_shouldInvokeKeyManagerClear() {
        service.logout();

        verify(keyManager, times(1)).clear();
    }
}