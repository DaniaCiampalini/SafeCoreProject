package com.safecore.business.exception;

/**
 * Eccezione lanciata quando una password non soddisfa i requisiti di sicurezza.
 */

public class WeakPasswordException extends SafeCoreException {
    public WeakPasswordException(String message) {
        super(message);
    }
}