package com.safecore.business.hints;

/**
 * Un suggerimento di sicurezza. 
 * Non è un errore cattivo che blocca tutto, ma un consiglio amichevole
 * per aiutare l'utente a creare una password degna di questo nome.
 */
public class PasswordHint {

    private final String message;
    private final HintLevel level;

    public PasswordHint(String message, HintLevel level) {
        this.message = message;
        this.level = level;
    }

    /**
     * Il testo del consiglio (es: "Manca un numero").
     */
    public String getMessage() {
        return message;
    }

    /**
     * Se è INFO o WARNING (serve per decidere il colore dell'etichetta nella UI).
     */
    public HintLevel getLevel() {
        return level;
    }
}