package com.safecore.security;

import org.springframework.stereotype.Component;

@Component
public class PasswordStrengthEvaluator {

    public enum Strength { WEAK, MEDIUM, STRONG }

    public Strength evaluate(String password) {
        if (password == null || password.length() < 6) return Strength.WEAK;

        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        int score = 0;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;

        if (score <= 2 || password.length() < 8) return Strength.WEAK;
        if (score == 3) return Strength.MEDIUM;
        return Strength.STRONG;
    }
}