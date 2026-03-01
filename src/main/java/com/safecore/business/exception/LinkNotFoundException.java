package com.safecore.business.exception;

/**
 * Eccezione lanciata quando un link SafeSend non viene trovato nel database.
 * Questo può accadere se il link è già stato utilizzato (burn-after-reading) o non è mai esistito.
 */
public class LinkNotFoundException extends SafeCoreException {

    /**
     * Crea una nuova LinkNotFoundException con il messaggio specificato.
     *
     * @param message il messaggio di errore che descrive il problema
     */
    public LinkNotFoundException(String message) {
        super(message);
    }
}

