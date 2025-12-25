package com.safecore.business.hints.rules;

import com.safecore.business.hints.PasswordHint;

import java.util.Optional;

/**
 * Regola di valutazione password.
 *
 * Strategy Pattern:
 * - Ogni regola valuta un aspetto
 */
public interface PasswordRule {

    Optional<PasswordHint> evaluate(String password);
}
