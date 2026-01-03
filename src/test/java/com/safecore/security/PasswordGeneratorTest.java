package com.safecore.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    void generate_correctLength() {
        String pwd = PasswordGenerator.generate(16);
        assertEquals(16, pwd.length());
    }

    @Test
    void generate_containsDifferentCharTypes() {
        String pwd = PasswordGenerator.generate(20);

        assertTrue(pwd.matches(".*[a-z].*"));
        assertTrue(pwd.matches(".*[A-Z].*"));
        assertTrue(pwd.matches(".*\\d.*"));
        assertTrue(pwd.matches(".*[^a-zA-Z0-9].*"));
    }
}
