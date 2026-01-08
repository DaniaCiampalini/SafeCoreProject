package com.safecore.business.exception;

public abstract class SafeCoreException extends RuntimeException {
    public SafeCoreException(String message) {
        super(message);
    }
}