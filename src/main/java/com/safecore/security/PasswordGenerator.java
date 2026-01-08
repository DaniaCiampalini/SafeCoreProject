package com.safecore.security;

import com.safecore.business.hints.rules.ComplexityRule;
import com.safecore.business.hints.rules.MinLengthRule;
import com.safecore.business.hints.rules.PasswordRule;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Generatore di password che si adegua dinamicamente alle regole di business.
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Allineamento con le regole esistenti
    private static final List<PasswordRule> RULES = new ArrayList<>();

    static {
        RULES.add(new MinLengthRule());
        RULES.add(new ComplexityRule());
    }

    private PasswordGenerator() {}

    /**
     * Genera una password valida basandosi sulle regole (Rules).
     */
    public static GeneratedPassword result(int requestedLength) {
        String password;
        boolean isValid;

        int finalLength = Math.max(requestedLength, 12);

        do {
            password = generate(finalLength);
            final String passToTest = password;

            // CORRETTO: Cambiato evaluate() in check() per allineamento interfaccia
            isValid = RULES.stream().allMatch(rule -> rule.check(passToTest).isEmpty());

        } while (!isValid || PasswordStrengthEvaluator.evaluate(password) != PasswordStrengthEvaluator.Strength.STRONG);

        return new GeneratedPassword(password, PasswordStrengthEvaluator.evaluate(password));
    }

    public static String generate(int length) {
        if (length < 4) length = 4; // Sicurezza minima per appendere i 4 tipi di char
        StringBuilder sb = new StringBuilder(length);

        sb.append(randomChar(LOWER));
        sb.append(randomChar(UPPER));
        sb.append(randomChar(DIGITS));
        sb.append(randomChar(SYMBOLS));

        for (int i = 4; i < length; i++) {
            sb.append(randomChar(ALL));
        }

        return shuffle(sb.toString());
    }

    private static char randomChar(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    public static class GeneratedPassword {
        private final String password;
        private final PasswordStrengthEvaluator.Strength strength;

        public GeneratedPassword(String password, PasswordStrengthEvaluator.Strength strength) {
            this.password = password;
            this.strength = strength;
        }

        public String getPassword() { return password; }
        public PasswordStrengthEvaluator.Strength getStrength() { return strength; }
    }
}