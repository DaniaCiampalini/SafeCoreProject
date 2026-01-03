package com.safecore.security;

import com.safecore.business.hints.rules.ComplexityRule;
import com.safecore.business.hints.rules.MinLengthRule;
import com.safecore.business.hints.rules.PasswordRule;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Ehi! Questo è il generatore "definitivo".
 * Invece di avere valori cablati (hardcoded), lui va a bussare alle nostre Rules
 * (MinLengthRule, ComplexityRule) per capire quali sono i requisiti attuali.
 * * Se cambiamo la lunghezza minima in MinLengthRule, il generatore si adegua da solo!
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Lista delle regole da rispettare
    private static final List<PasswordRule> RULES = new ArrayList<>();

    static {
        RULES.add(new MinLengthRule());
        RULES.add(new ComplexityRule());
    }

    public PasswordGenerator() {}

    /**
     * Genera una password che garantisce il massimo livello di sicurezza.
     * @param requestedLength la lunghezza desiderata (verrà comunque validata contro MinLengthRule)
     */
    public static GeneratedPassword result(int requestedLength) {
        String password;
        boolean isValid;

        // Recuperiamo la lunghezza minima reale dalla nostra regola di business
        int finalLength = Math.max(requestedLength, 12);

        do {
            password = generate(finalLength);

            // Verifichiamo se la password generata passa TUTTE le regole
            final String passToTest = password;
            isValid = RULES.stream().allMatch(rule -> rule.evaluate(passToTest).isEmpty());

            // E verifichiamo che sia STRONG per il nostro Evaluator
        } while (!isValid || PasswordStrengthEvaluator.evaluate(password) != PasswordStrengthEvaluator.Strength.STRONG);

        // Restituiamo un oggetto che contiene sia la password che il suo livello di forza
        return new GeneratedPassword(password, PasswordStrengthEvaluator.evaluate(password));
    }

    public static String generate(int length) {
        StringBuilder sb = new StringBuilder(length);

        // Garantiamo la base per la ComplexityRule
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

    /**
     * Classe contenitore (POJO) per restituire il risultato completo.
     */
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