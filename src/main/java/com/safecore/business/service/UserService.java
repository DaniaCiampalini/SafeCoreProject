package com.safecore.business.service;

import com.safecore.business.domain.User;

import java.util.Optional;

/**
 * Interfaccia per la gestione degli utenti (registrazione e login).
 * Usiamo un'interfaccia così se un domani decidiamo di cambiare il modo in cui
 * gestiamo gli utenti (magari passando a un sistema esterno come OAuth),
 * il resto dell'app non se ne accorge neanche.
 */
public interface UserService {
    /**
     * Crea un nuovo account.
     */
    User register(String email, String plainPassword);

    /**
     * Tenta il login e restituisce l'utente se le credenziali sono corrette.
     */
    Optional<User> login(String email, String plainPassword);
}