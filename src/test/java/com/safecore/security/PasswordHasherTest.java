package com.safecore.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashAndVerify_success() {
        String password = "Secure123!";
        String hash = PasswordHasher.hash(password);

        assertNotNull(hash);
        assertTrue(PasswordHasher.verify(password, hash));
    }

    @Test
    void verify_wrongPassword_fails() {
        String hash = PasswordHasher.hash("Correct123!");
        assertFalse(PasswordHasher.verify("Wrong123!", hash));
    }

    @Test
    void hash_blankPassword_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> PasswordHasher.hash(""));
    }
}
