package com.safecore.business.exception;

/**
 * La "mamma" di tutte le nostre eccezioni personalizzate.
 * Estende RuntimeException così non siamo obbligati a dichiararle sempre (le catturiamo
 * in modo centralizzato nel GlobalExceptionHandler).
 */
public abstract class SafeCoreException extends RuntimeException {
    public SafeCoreException(String message) {
        super(message);
    }
}