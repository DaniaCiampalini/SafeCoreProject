package com.safecore.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordStrengthEvaluatorTest {

    @Test
    void weakPassword_detected() {
        assertEquals(
                PasswordStrengthEvaluator.Strength.WEAK,
                PasswordStrengthEvaluator.evaluate("abc")
        );
    }

    @Test
    void mediumPassword_detected() {
        assertEquals(
                PasswordStrengthEvaluator.Strength.MEDIUM,
                PasswordStrengthEvaluator.evaluate("Abcdef12")
        );
    }

    @Test
    void strongPassword_detected() {
        assertEquals(
                PasswordStrengthEvaluator.Strength.STRONG,
                PasswordStrengthEvaluator.evaluate("Str0ng!Pass")
        );
    }
}
