package com.safecore.business.service;

import java.time.LocalDateTime;

/**
 * Evento di dominio emesso al termine di un reset password riuscito.
 */
public class PasswordResetCompletedEvent {

    private final String email;
    private final LocalDateTime completedAt;

    public PasswordResetCompletedEvent(String email, LocalDateTime completedAt) {
        this.email = email;
        this.completedAt = completedAt;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
