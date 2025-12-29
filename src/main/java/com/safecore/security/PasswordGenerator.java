package com.safecore.security;

import java.security.SecureRandom;

/**
 * Utility per la generazione di password sicure.
 *
 * Scelte di Sicurezza:
 * - SecureRandom (non Random)
 * - Set di caratteri bilanciato
 *
 * Scelte SE:
 * - Classe utility stateless
 * - Riutilizzabile da Register e Reset
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}";

    private static final String ALL =
            LOWER + UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
        // utility class
    }

    /**
     * Genera una password sicura.
     *
     * @param length lunghezza minima consigliata >= 8
     */
    public static String generate(int length) {

        if (length < 8) {
            throw new IllegalArgumentException(
                    "Password length must be at least 8");
        }

        StringBuilder password = new StringBuilder(length);

        // Garantiamo almeno un carattere per categoria
        password.append(randomChar(LOWER));
        password.append(randomChar(UPPER));
        password.append(randomChar(DIGITS));
        password.append(randomChar(SYMBOLS));

        // Resto casuale
        for (int i = 4; i < length; i++) {
            password.append(randomChar(ALL));
        }

        return shuffle(password.toString());
    }

    private static char randomChar(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }

    /**
     * Mischia i caratteri per evitare pattern iniziali fissi.
     */
    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int j = RANDOM.nextInt(chars.length);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
