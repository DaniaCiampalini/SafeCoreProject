package com.safecore.business.exception;

/**
 * Lanciata quando la password scelta dall'utente fa piangere il nostro PasswordStrengthEvaluator.
 * Non accettiamo password deboli, punto.
 */
public class WeakPasswordException extends SafeCoreException {
    public WeakPasswordException(String message) {
        super(message);
    }
}