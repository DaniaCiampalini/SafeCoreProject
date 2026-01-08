package com.safecore.business.exception;

public class UserNotFoundException extends SafeCoreException {
    public UserNotFoundException(String email) {
        super("Utente non trovato per l'email: " + email);
    }
}