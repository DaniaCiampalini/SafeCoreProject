package com.safecore.business.service;

import com.safecore.business.domain.User;
import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.business.service.impl.UserServiceImpl;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.KeyManager;
import com.safecore.security.PasswordHasher;
import com.safecore.security.PasswordStrengthEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordHasher passwordHasher;
    private PasswordStrengthEvaluator strengthEvaluator;
    private KeyManager keyManager;
    private PasswordEntryRepository passwordEntryRepository;
    private UserService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordHasher = mock(PasswordHasher.class);
        strengthEvaluator = mock(PasswordStrengthEvaluator.class);
        keyManager = mock(KeyManager.class);
        passwordEntryRepository = mock(PasswordEntryRepository.class);
        service = new UserServiceImpl(userRepository, passwordHasher, strengthEvaluator, keyManager, passwordEntryRepository);
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

    // ========== Test per secureDeleteAccount ==========

    @Test
    void secureDeleteAccount_withValidPassword_deletesUserAndEntries() {
        String email = "delete@test.com";
        String password = "Password123!";
        byte[] salt = new byte[32];

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setDerivationSalt(salt);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(password, "hashed_password")).thenReturn(true);
        when(passwordEntryRepository.findByUserEmail(email)).thenReturn(Collections.emptyList());

        service.secureDeleteAccount(email, password);

        verify(userRepository).delete(user);
        verify(keyManager).clear();
    }

    @Test
    void secureDeleteAccount_withInvalidPassword_throwsInvalidTokenException() {
        String email = "test@safecore.com";
        String wrongPassword = "WrongPassword123!";
        byte[] salt = new byte[32];

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setDerivationSalt(salt);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(wrongPassword, "hashed_password")).thenReturn(false);

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> service.secureDeleteAccount(email, wrongPassword)
        );

        assertTrue(exception.getMessage().contains("Password errata"));
        verify(userRepository, never()).delete(any());
        verify(keyManager, never()).clear();
    }

    @Test
    void secureDeleteAccount_withNonExistentUser_throwsUserNotFoundException() {
        String email = "nonexistent@test.com";
        String password = "Password123!";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> service.secureDeleteAccount(email, password)
        );

        assertTrue(exception.getMessage().contains("Utente non trovato"));
        verify(passwordHasher, never()).verify(anyString(), anyString());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void secureDeleteAccount_withPasswordEntries_sanitizesAndDeletesThem() {
        String email = "user@test.com";
        String password = "Password123!";
        byte[] salt = new byte[32];

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setDerivationSalt(salt);

        // Crea entry con dati sensibili
        PasswordEntryEntity entry1 = new PasswordEntryEntity();
        entry1.setId(UUID.randomUUID());
        entry1.setServiceName("Gmail");
        entry1.setUsername("user@gmail.com");
        entry1.setEncryptedPassword(new byte[]{1, 2, 3, 4, 5});

        PasswordEntryEntity entry2 = new PasswordEntryEntity();
        entry2.setId(UUID.randomUUID());
        entry2.setServiceName("Facebook");
        entry2.setUsername("user@fb.com");
        entry2.setEncryptedPassword(new byte[]{6, 7, 8, 9, 10});

        List<PasswordEntryEntity> entries = Arrays.asList(entry1, entry2);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(password, "hashed_password")).thenReturn(true);
        when(passwordEntryRepository.findByUserEmail(email)).thenReturn(entries);

        service.secureDeleteAccount(email, password);

        // Verifica sanitizzazione
        verify(passwordEntryRepository).saveAll(anyList());
        verify(passwordEntryRepository, times(2)).flush();

        // Verifica eliminazione
        verify(passwordEntryRepository).deleteAll(entries);
        verify(userRepository).delete(user);
        verify(keyManager).clear();
    }

    @Test
    void secureDeleteAccount_ensuresTransactionalIntegrity() {
        String email = "test@example.com";
        String password = "Password123!";
        byte[] salt = new byte[32];

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed_password");
        user.setDerivationSalt(salt);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordHasher.verify(password, "hashed_password")).thenReturn(true);
        when(passwordEntryRepository.findByUserEmail(email)).thenReturn(Collections.emptyList());

        service.secureDeleteAccount(email, password);

        // Verifica che le operazioni siano chiamate nell'ordine corretto
        verify(userRepository).findByEmail(email);
        verify(passwordHasher).verify(password, "hashed_password");
        verify(passwordEntryRepository).findByUserEmail(email);
        verify(userRepository).save(user);
        verify(userRepository, atLeast(1)).flush(); // Flush chiamato almeno una volta
        verify(userRepository).delete(user); // Eliminazione
        verify(keyManager).clear();
    }
}
