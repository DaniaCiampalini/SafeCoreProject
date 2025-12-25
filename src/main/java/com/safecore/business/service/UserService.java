package com.safecore.business.service;

import com.safecore.business.domain.User;

import java.util.Optional;

/**
 * Service Layer per la gestione utenti.
 *
 * Scelte SE:
 * - Interfaccia → disaccoppiamento
 * - Espone casi d'uso, NON CRUD
 */
public interface UserService {

    User register(String email, String plainPassword);

    Optional<User> login(String email, String plainPassword);
}
