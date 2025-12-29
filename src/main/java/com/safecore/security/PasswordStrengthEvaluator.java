package com.safecore.security;

/**
 * Utility per valutare la robustezza di una password.
 *
 * NON è crittografia, ma valutazione euristica lato UI.
 */
public final class PasswordStrengthEvaluator {

    private PasswordStrengthEvaluator() {
    }

    public enum Strength {
        WEAK,
        MEDIUM,
        STRONG
    }

    public static Strength evaluate(String password) {

        if (password == null || password.isBlank()) {
            return Strength.WEAK;
        }

        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        int score = 0;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;

        if (score <= 1) return Strength.WEAK;
        if (score <= 3) return Strength.MEDIUM;
        return Strength.STRONG;
    }
}
