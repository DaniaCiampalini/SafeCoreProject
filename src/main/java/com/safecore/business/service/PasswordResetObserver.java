package com.safecore.business.service;

/**
 * Observer notificato quando un reset password è stato completato con successo.
 */
public interface PasswordResetObserver {

    void onPasswordResetCompleted(PasswordResetCompletedEvent event);
}
