package com.safecore.security;

import org.springframework.stereotype.Component;

/**
 * Questo è il nostro "giudice" delle password. 
 * Analizza la stringa e decide se è troppo debole per essere accettata.
 * Non guarda solo la lunghezza, ma anche la varietà di caratteri.
 */
@Component
public class PasswordStrengthEvaluator {

    // I tre possibili giudizi
    public enum Strength { WEAK, MEDIUM, STRONG }

    /**
     * Valuta la forza di una password.
     */
    public Strength evaluate(String password) {
        // Se è troppo corta o nulla, bocciata subito.
        if (password == null || password.length() < 6) return Strength.WEAK;

        // Controlliamo che tipi di caratteri ha dentro
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSymbol = password.matches(".*[^a-zA-Z0-9].*");

        // Diamo un punto per ogni categoria trovata
        int score = 0;
        if (hasLower) score++;
        if (hasUpper) score++;
        if (hasDigit) score++;
        if (hasSymbol) score++;

        // Logica di valutazione:
        // - Meno di 3 categorie o meno di 8 caratteri? Debole.
        // - 3 categorie? Mediocre.
        // - Tutti e 4 i tipi di caratteri e lunga a sufficienza? Forte!
        if (score <= 2 || password.length() < 8) return Strength.WEAK;
        if (score == 3) return Strength.MEDIUM;
        return Strength.STRONG;
    }
}