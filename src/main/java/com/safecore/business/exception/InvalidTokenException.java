package com.safecore.business.exception;

/**
 * Lanciata quando il token di reset password è sbagliato, scaduto o già usato.
 */
public class InvalidTokenException extends SafeCoreException {
    public InvalidTokenException(String message) {
        super(message);
    }
}