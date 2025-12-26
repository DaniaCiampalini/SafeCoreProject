package com.safecore.security;

import java.security.SecureRandom;

/**
 * Utility per la generazione di password sicure.
 *
 * Sicurezza:
 * - SecureRandom (non Random)
 * - Mix di caratteri
 *
 * SE:
 * - Classe utility stateless
 * - Riutilizzabile da più controller
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";

    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom random = new SecureRandom();

    private PasswordGenerator() {
        // Utility class
    }

    /**
     * Genera una password sicura.
     *
     * @param length lunghezza password (>= 12 consigliato)
     */
    public static String generate(int length) {

        if (length < 8) {
            throw new IllegalArgumentException("Password length must be >= 8");
        }

        StringBuilder sb = new StringBuilder(length);

        // Garantiamo almeno un carattere per categoria
        sb.append(randomChar(LOWER));
        sb.append(randomChar(UPPER));
        sb.append(randomChar(DIGITS));
        sb.append(randomChar(SYMBOLS));

        // Completiamo il resto
        for (int i = 4; i < length; i++) {
            sb.append(randomChar(ALL));
        }

        // Mischiamo i caratteri
        return shuffle(sb.toString());
    }

    private static char randomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
