package com.safecore.business.service;

import com.safecore.business.hints.PasswordHint;
import com.safecore.business.hints.rules.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordHintService {

    private final List<PasswordRule> rules = new ArrayList<>();

    public PasswordHintService() {
        // Configuriamo le strategie di controllo
        rules.add(new MinLengthRule());
        rules.add(new ComplexityRule());
    }

    /**
     * Analizza la password e restituisce i consigli.
     * Ho rinominato in getHints per coerenza con i tuoi test precedenti,
     * o puoi rinominare il test in 'evaluate'.
     */
    public List<PasswordHint> getHints(String password) {
        List<PasswordHint> hints = new ArrayList<>();
        for (PasswordRule rule : rules) {
            rule.check(password).ifPresent(hints::add);
        }
        return hints;
    }
}