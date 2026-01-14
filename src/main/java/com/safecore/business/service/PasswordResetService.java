package com.safecore.business.service;

/**
 * Interfaccia per il reset della password dimenticata.
 */
public interface PasswordResetService {

    /**
     * Genera un token per permettere il reset restituendo anche la scadenza.
     */
    PasswordResetRequestResult requestReset(String email);

    /**
     * Cambia effettivamente la password se il token è corretto.
     */
    void resetPassword(String email, String token, String newPassword);
}
