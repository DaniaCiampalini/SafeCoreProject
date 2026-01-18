package com.safecore.business.service;

/**
 * Interfaccia per il reset della password dimenticata.
 * Fornisce metodi per richiedere un reset e per eseguire il reset effettivo.
 */
public interface PasswordResetService {

    // Genera un token per permettere il reset restituendo anche la scadenza.
    PasswordResetRequestResult requestReset(String email);

    void resetPassword(String email, String token, String newPassword);
}
