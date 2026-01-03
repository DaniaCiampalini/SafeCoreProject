package com.safecore.business.hints;

/**
 * Questa classe rappresenta un suggerimento di sicurezza.
 * L'ho pensata come un oggetto "informativo": non blocca l'utente (non è un'eccezione),
 * ma serve a guidarlo verso una password migliore.
 */
public class PasswordHint {

    private final String message;
    private final HintLevel level;

    public PasswordHint(String message, HintLevel level) {
        this.message = message;
        this.level = level;
    }

    // Utilizzati dalla UI per mostrare il testo all'utente
    public String getMessage() {
        return message;
    }

    // Utilizzato dalla UI per decidere il colore (es. INFO=blu, WARNING=giallo)
    public HintLevel getLevel() {
        return level;
    }
}