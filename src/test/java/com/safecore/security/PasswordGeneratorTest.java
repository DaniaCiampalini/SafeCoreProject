package com.safecore.security;

import com.safecore.business.hints.rules.PasswordRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordGeneratorTest {

    // Istanziamo il generatore con i suoi componenti reali o mock
    private final PasswordStrengthEvaluator evaluator = new PasswordStrengthEvaluator();
    private final List<PasswordRule> rules = new ArrayList<>(); // Lista vuota o con regole reali
    private final PasswordGenerator generator = new PasswordGenerator(rules, evaluator);

    @Test
    void generateSafe_correctLength() {
        String pwd = generator.generateSafe(16);
        assertEquals(16, pwd.length());
    }
}