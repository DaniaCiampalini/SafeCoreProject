package com.safecore.business.exception;

/**
 * Eccezione lanciata quando qualcuno cerca un utente che non esiste nel DB.
 */
public class UserNotFoundException extends SafeCoreException {
    public UserNotFoundException(String email) {
        super("Spiacente, non ho trovato nessun utente con l'email: " + email);
    }
}