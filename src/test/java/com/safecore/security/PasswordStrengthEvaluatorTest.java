package com.safecore.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordStrengthEvaluatorTest {

    private final PasswordStrengthEvaluator evaluator = new PasswordStrengthEvaluator();

    @Test
    void weakPassword_detected() {
        assertEquals(PasswordStrengthEvaluator.Strength.WEAK, evaluator.evaluate("abc"));
    }

    @Test
    void strongPassword_detected() {
        assertEquals(PasswordStrengthEvaluator.Strength.STRONG, evaluator.evaluate("Str0ng!Pass2026"));
    }
}