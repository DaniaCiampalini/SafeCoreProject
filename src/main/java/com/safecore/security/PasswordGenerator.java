package com.safecore.security;

import com.safecore.business.hints.rules.PasswordRule;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

/**
 * Questo è il nostro "chef" delle password.
 * Non si limita a buttare caratteri a caso, ma segue una ricetta precisa per
 * assicurarsi che la password sia digeribile (sicura) per i nostri standard.
 */
@Component
public class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    // SecureRandom è molto meglio di Random per la crittografia (è meno prevedibile)
    private final SecureRandom random = new SecureRandom();
    private final List<PasswordRule> rules;
    private final PasswordStrengthEvaluator evaluator;

    public PasswordGenerator(List<PasswordRule> rules, PasswordStrengthEvaluator evaluator) {
        this.rules = rules;
        this.evaluator = evaluator;
    }

    /**
     * Genera una password che siamo sicuri passerà tutti i nostri test di sicurezza.
     */
    public String generateSafe(int length) {
        String password;
        boolean isValid;
        int finalLength = Math.max(length, 12); // Almeno 12 caratteri, altrimenti non è seria

        // Continuiamo a generare finché non ne troviamo una che ci piace davvero
        do {
            password = generateRaw(finalLength);
            String current = password;
            // Controlliamo se rispetta tutte le regole (MinLength, Complexity, ecc.)
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
        // Assicuriamoci di avere un po' di tutto
        sb.append(randomChar(LOWER));
        sb.append(randomChar(UPPER));
        sb.append(randomChar(DIGITS));
        sb.append(randomChar(SYMBOLS));

        // Completiamo la lunghezza richiesta
        for (int i = 4; i < length; i++) {
            sb.append(randomChar(ALL));
        }
        // Mischiamo il tutto così i caratteri garantiti non sono sempre all'inizio
        return shuffle(sb.toString());
    }

    private char randomChar(String s) {
        return s.charAt(random.nextInt(s.length()));
    }

    /**
     * Algoritmo di Fisher-Yates per mescolare la stringa in modo equo.
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