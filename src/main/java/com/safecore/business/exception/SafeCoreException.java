package com.safecore.business.exception;

/**
 * Eccezione base per tutte le eccezioni specifiche di SafeCore.
 */
public abstract class SafeCoreException extends RuntimeException {
    public SafeCoreException(String message) {
        super(message);
    }
}