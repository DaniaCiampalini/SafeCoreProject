package com.safecore.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(); // Istanza fisica per unit test

    @Test
    void hashAndVerify_success() {
        String password = "Secure123!";
        String hash = hasher.hash(password);

        assertNotNull(hash);
        assertTrue(hasher.verify(password, hash));
    }

    @Test
    void verify_wrongPassword_fails() {
        String hash = hasher.hash("Correct123!");
        assertFalse(hasher.verify("Wrong123!", hash));
    }
}