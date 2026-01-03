package com.safecore.business.hints;

import com.safecore.business.hints.rules.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Questo è il motore che genera i suggerimenti.
 * Ho usato lo Strategy Pattern: il service coordina una lista di regole.
 * Se un domani volessimo aggiungere una regola "Password non deve contenere il tuo nome",
 * basta creare una nuova classe e aggiungerla qui senza rompere nulla.
 */
public class PasswordHintService {

    private final List<PasswordRule> rules = new ArrayList<>();

    public PasswordHintService() {
        // Qui configuriamo le regole attive
        rules.add(new MinLengthRule());
        rules.add(new ComplexityRule());
    }

    /**
     * Passagli la password che l'utente sta scrivendo e ti restituirà
     * una lista di consigli (se ce ne sono).
     */
    public List<PasswordHint> evaluate(String password) {
        List<PasswordHint> hints = new ArrayList<>();
        for (PasswordRule rule : rules) {
            rule.evaluate(password).ifPresent(hints::add);
        }
        return hints;
    }
}