package com.safecore.business.hints;

/**
 * Rappresenta un suggerimento di sicurezza.
 *
 * NON è un errore bloccante.
 * È informativo e non vincolante.
 */
public class PasswordHint {

    private final String message;
    private final HintLevel level;

    public PasswordHint(String message, HintLevel level) {
        this.message = message;
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public HintLevel getLevel() {
        return level;
    }
}
