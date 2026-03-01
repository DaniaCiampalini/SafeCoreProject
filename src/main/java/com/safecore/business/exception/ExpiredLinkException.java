package com.safecore.business.exception;

/**
 * Eccezione lanciata quando si tenta di accedere a un link SafeSend scaduto.
 * Il link viene automaticamente eliminato dal sistema quando scaduto.
 */
public class ExpiredLinkException extends SafeCoreException {

    /**
     * Crea una nuova ExpiredLinkException con il messaggio specificato.
     *
     * @param message il messaggio di errore che descrive il problema
     */
    public ExpiredLinkException(String message) {
        super(message);
    }
}

