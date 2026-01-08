package com.safecore.business.exception;

public class UserAlreadyExistsException extends SafeCoreException {
    public UserAlreadyExistsException(String email) {
        super("L'account con email " + email + " esiste già.");
    }
}