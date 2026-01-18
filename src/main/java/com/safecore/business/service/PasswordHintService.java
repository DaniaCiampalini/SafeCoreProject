package com.safecore.business.service;

import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;
import com.safecore.business.hints.rules.PasswordRule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servizio che analizza la robustezza delle password tramite un sistema a regole.
 * Utilizza il pattern Strategy (tramite la lista di PasswordRule) per validare l'input.
 */

@Service
public class PasswordHintService {

    private final List<PasswordRule> rules;

    public PasswordHintService(List<PasswordRule> rules) {
        this.rules = rules;
    }

    /**
     * Analizza la password e restituisce un riassunto della sua sicurezza.
     * Se trova dei WARNING, restituisce il primo trovato.
     * Altrimenti restituisce un feedback positivo (INFO).
     */
    public PasswordHint evaluatePassword(String password) {
        List<PasswordHint> allHints = getHints(password);

        Optional<PasswordHint> worstHint = allHints.stream()
                .filter(h -> h.getLevel() == HintLevel.WARNING)
                .findFirst();

        if (worstHint.isPresent()) {
            return worstHint.get();
        }

        return new PasswordHint("La password rispetta i criteri di sicurezza.", HintLevel.INFO);
    }

    /**
     * Restituisce la lista completa di tutti i suggerimenti rilevati dalle regole.
     */
    public List<PasswordHint> getHints(String password) {
        List<PasswordHint> hints = new ArrayList<>();
        if (password == null || password.isEmpty()) {
            hints.add(new PasswordHint("La password non può essere vuota.", HintLevel.WARNING));
            return hints;
        }

        for (PasswordRule rule : rules) {
            rule.check(password).ifPresent(hints::add);
        }
        return hints;
    }
}