package com.safecore.security;

import java.security.SecureRandom;

/**
 * Utility per la generazione di password sicure.
 *
 * Scelte di sicurezza:
 * - SecureRandom (non Random)
 * - Mix di lettere, numeri e simboli
 *
 * Scelte progettuali:
 * - Classe utility (stateless)
 * - Nessuna dipendenza da UI o business
 */
public final class PasswordGenerator {

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+";

    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordGenerator() {
        // utility class
    }

    /**
     * Genera una password sicura.
     *
     * @param length lunghezza (consigliato ≥ 12)
     */
    public static String generate(int length) {

        if (length < 8) {
            throw new IllegalArgumentException("Password length must be >= 8");
        }

        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALL.length());
            password.append(ALL.charAt(index));
        }

        return password.toString();
    }
}
