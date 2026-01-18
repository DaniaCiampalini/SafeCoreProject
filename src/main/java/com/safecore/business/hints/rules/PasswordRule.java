package com.safecore.business.hints.rules;

import com.safecore.business.hints.PasswordHint;

import java.util.Optional;

/**
 * Interfaccia base per una regola sulla password.
 * Se si vuole aggiungere un nuovo controllo,
 * basta annotare l'implementazione con @Component.
 */

public interface PasswordRule {
    Optional<PasswordHint> check(String password);
}