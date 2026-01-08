package com.safecore.security;

import com.safecore.business.hints.rules.PasswordRule;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.List;

@Component
public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    private final SecureRandom random = new SecureRandom();
    private final List<PasswordRule> rules;
    private final PasswordStrengthEvaluator evaluator;

    public PasswordGenerator(List<PasswordRule> rules, PasswordStrengthEvaluator evaluator) {
        this.rules = rules;
        this.evaluator = evaluator;
    }

    public String generateSafe(int length) {
        String password;
        boolean isValid;
        int finalLength = Math.max(length, 12);

        do {
            password = generateRaw(finalLength);
            String current = password;
            isValid = rules.stream().allMatch(r -> r.check(current).isEmpty())
                    && evaluator.evaluate(current) == PasswordStrengthEvaluator.Strength.STRONG;
        } while (!isValid);

        return password;
    }

    private String generateRaw(int length) {
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

    private char randomChar(String s) { return s.charAt(random.nextInt(s.length())); }

    private String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}