package com.safecore.business.hints.rules;

import com.safecore.business.hints.PasswordHint;

import java.util.Optional;

/**
 * Interfaccia base per una regola sulla password.
 * Se vuoi aggiungere un nuovo controllo (es: "niente parole nel dizionario"),
 * ti basta implementare questa interfaccia e annotarla con @Component.
 */
public interface PasswordRule {
    /**
     * Controlla se la password rispetta la regola.
     * Restituisce un Optional con il suggerimento se la regola è violata,
     * altrimenti un Optional vuoto se è tutto ok.
     */
    Optional<PasswordHint> check(String password);
}