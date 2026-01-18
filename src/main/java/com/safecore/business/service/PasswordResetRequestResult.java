package com.safecore.business.service;

import java.time.LocalDateTime;

/**
 * Risultato della richiesta di reset della password.
 */
public class PasswordResetRequestResult {

    private final String token;
    private final LocalDateTime expiresAt;

    public PasswordResetRequestResult(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
