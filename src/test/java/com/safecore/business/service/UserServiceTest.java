package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService service;

    @BeforeEach
    void setUp() {
        // Mock del repository Spring
        userRepository = mock(UserRepository.class);
        service = new UserServiceImpl(userRepository);
    }

    @Test
    void registerAndLogin_success() {
        String email = "test@mail.com";
        String pass = "Password123!";

        // Simuliamo che l'utente non esista ancora
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Simuliamo il comportamento del salvataggio
        UserEntity savedEntity = new UserEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail(email);
        savedEntity.setPasswordHash(PasswordHasher.hash(pass));

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);
        // Simuliamo il ritrovamento dell'utente per il login
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(savedEntity));

        // Test Registrazione
        User registered = service.register(email, pass);
        assertNotNull(registered);
        assertEquals(email, registered.getEmail());

        // Test Login
        Optional<User> loggedUser = service.login(email, pass);
        assertTrue(loggedUser.isPresent());
        assertEquals(email, loggedUser.get().getEmail());
    }

    @Test
    void register_duplicateEmail_fails() {
        String email = "duplicate@mail.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.register(email, "Password123!"));
    }

    @Test
    void login_wrongPassword_returnsEmpty() {
        String email = "user@mail.com";
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setPasswordHash(PasswordHasher.hash("CorrectPass123!"));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(entity));

        Optional<User> result = service.login(email, "WrongPassword");
        assertTrue(result.isEmpty(), "Il login deve fallire con password errata");
    }
}