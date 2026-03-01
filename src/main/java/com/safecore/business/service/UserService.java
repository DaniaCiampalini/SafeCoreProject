package com.safecore.business.service;

import com.safecore.business.domain.User;

import java.util.Optional;

/**
 * Servizio per la gestione degli utenti.
 * Fornisce metodi per registrazione e login.
 *
 */
public interface UserService {
    User register(String email, String plainPassword);

    // Restituisce l'utente se le credenziali sono corrette.
    Optional<User> login(String email, String plainPassword);

    public void logout();

    /**
     * Elimina in modo sicuro l'account dell'utente dopo aver verificato la password.
     * Sovrascrive i dati sensibili prima della cancellazione fisica.
     *
     * @param email l'email dell'utente
     * @param plainPassword la password in chiaro per la riautenticazione
     * @throws com.safecore.business.exception.UserNotFoundException se l'utente non esiste
     * @throws com.safecore.business.exception.InvalidTokenException se la password è errata
     */
    void secureDeleteAccount(String email, String plainPassword);
}
