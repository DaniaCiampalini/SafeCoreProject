package com.safecore.business.hints;

import com.safecore.business.hints.rules.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Service per la generazione degli Smart Hints.
 *
 * Responsabilità:
 * - Coordina le regole
 * - NON decide al posto dell'utente
 */
public class PasswordHintService {

    private final List<PasswordRule> rules = new ArrayList<>();

    public PasswordHintService() {
        // Strategy configurabile
        rules.add(new MinLengthRule());
        rules.add(new ComplexityRule());
    }

    /**
     * Valuta una password e restituisce suggerimenti.
     */
    public List<PasswordHint> evaluate(String password) {

        List<PasswordHint> hints = new ArrayList<>();

        for (PasswordRule rule : rules) {
            rule.evaluate(password).ifPresent(hints::add);
        }

        return hints;
    }
}
