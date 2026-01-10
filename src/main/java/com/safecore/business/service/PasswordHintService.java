package com.safecore.business.service;

import com.safecore.business.hints.PasswordHint;
import com.safecore.business.hints.rules.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Questo servizio analizza la password mentre la scrivi e ti dà dei consigli (hint)
 * su come migliorarla. È basato su un sistema a regole estensibile.
 */
@Service
public class PasswordHintService {

    // Spring è magico: qui inietta automaticamente TUTTE le classi che implementano PasswordRule.
    // Se domani aggiungiamo una nuova regola, basterà creare la classe e Spring la metterà qui.
    private final List<PasswordRule> rules;

    public PasswordHintService(List<PasswordRule> rules) {
        this.rules = rules;
    }

    /**
     * Controlla la password passandola sotto tutte le regole configurate.
     * Restituisce una lista di consigli (es: "Manca una maiuscola").
     */
    public List<PasswordHint> getHints(String password) {
        List<PasswordHint> hints = new ArrayList<>();
        if (password == null) return hints;

        for (PasswordRule rule : rules) {
            // Se la regola trova un problema, aggiungiamo il consiglio alla lista
            rule.check(password).ifPresent(hints::add);
        }
        return hints;
    }
}