package com.safecore.security;

import com.safecore.business.hints.rules.PasswordRule;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

/**
 * Generatore di password sicure.
 * Utilizza regole di sicurezza e un valutatore di robustezza per creare password che
 * rispettano i criteri di sicurezza.
 */
@Component
public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    // SecureRandom meglio di Random per la crittografia perché più imprevedibile
    private final SecureRandom random = new SecureRandom();
    private final List<PasswordRule> rules;
    private final PasswordStrengthEvaluator evaluator;

    public PasswordGenerator(List<PasswordRule> rules, PasswordStrengthEvaluator evaluator) {
        this.rules = rules;
        this.evaluator = evaluator;
    }

    /**
     * Genera una password sicura che rispetta tutte le regole e ha una robustezza elevata.
     */
    public String generateSafe(int length) {
        String password;
        boolean isValid;
        int finalLength = Math.max(length, 16);


        do {
            password = generateRaw(finalLength);
            String current = password;
            isValid = rules.stream().allMatch(r -> r.check(current).isEmpty())
                    && evaluator.evaluate(current) == PasswordStrengthEvaluator.Strength.STRONG;
        } while (!isValid);

        return password;
    }

    /**
     * Crea una stringa casuale con almeno un carattere per tipo.
     */
    private String generateRaw(int length) {
        StringBuilder sb = new StringBuilder(length);
        sb.append(randomChar(LOWER));
        sb.append(randomChar(UPPER));
        sb.append(randomChar(DIGITS));
        sb.append(randomChar(SYMBOLS));

        for (int i = 4; i < length; i++) {
            sb.append(randomChar(ALL));
        }
        // Mescola per evitare schemi prevedibili
        return shuffle(sb.toString());
    }

    private char randomChar(String s) {
        return s.charAt(random.nextInt(s.length()));
    }

    /**
     * Algoritmo di Fisher-Yates per mescolare la stringa in modo equo.
     * Garantisce che la password non abbia schemi prevedibili.
     */
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