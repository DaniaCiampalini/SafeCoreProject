package com.safecore.business.exception;

/**
 * Lanciata se qualcuno prova a registrarsi con un'email che abbiamo già in archivio.
 */
public class UserAlreadyExistsException extends SafeCoreException {
    public UserAlreadyExistsException(String email) {
        super("Ehilà! Un account con l'email " + email + " esiste già. Prova a fare il login.");
    }
}