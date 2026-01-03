package com.safecore.business.service;

import com.safecore.business.domain.User;
import java.util.Optional;

/**
 * Questa è l'interfaccia principale per gestire gli utenti.
 * Ho scelto di usare un'interfaccia così se un domani passiamo da un DB locale
 * a un'API esterna, ci basta cambiare l'implementazione senza toccare la UI.
 */
public interface UserService {
    // Restituisce l'utente creato (utile per loggarlo subito dopo la registrazione)
    User register(String email, String plainPassword);

    // Restituisce un Optional: se le credenziali sono errate, l'Optional è vuoto.
    Optional<User> login(String email, String plainPassword);
}