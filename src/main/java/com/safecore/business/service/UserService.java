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
}