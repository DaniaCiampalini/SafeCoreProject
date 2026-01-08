package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private PasswordStrengthEvaluator strengthEvaluator;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        strengthEvaluator = mock(PasswordStrengthEvaluator.class);
        service = new UserServiceImpl(userRepository, passwordHasher, strengthEvaluator);
    }

    @Test
    void registerAndLogin_success() {
        String email = "test@mail.com";
        String pass = "Password123!";

        when(strengthEvaluator.evaluate(pass)).thenReturn(PasswordStrengthEvaluator.Strength.STRONG);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordHasher.hash(pass)).thenReturn("hashed_pass");

        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail(email);
        savedEntity.setPasswordHash("hashed_pass");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedEntity));
        when(passwordHasher.verify(pass, "hashed_pass")).thenReturn(true);

        User registered = service.register(email, pass);
        assertNotNull(registered);

        Optional<User> loggedUser = service.login(email, pass);
        assertTrue(loggedUser.isPresent());
    }
}