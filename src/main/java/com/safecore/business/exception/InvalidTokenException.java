package com.safecore.business.exception;

/**
 * Eccezione lanciata quando un token SafeSend non è valido o non corrisponde all'hash memorizzato.
 * Utilizzata per proteggere i link di condivisione sicura da accessi non autorizzati.
 */
public class InvalidTokenException extends SafeCoreException {

    /**
     * Crea una nuova InvalidTokenException con il messaggio specificato.
     *
     * @param message il messaggio di errore che descrive il problema
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}

