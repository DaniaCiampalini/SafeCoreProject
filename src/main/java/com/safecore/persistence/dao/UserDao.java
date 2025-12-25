package com.safecore.persistence.dao;

import com.safecore.business.domain.User;

import java.util.Optional;

/**
 * DAO per l'accesso ai dati User.
 *
 * Scelte SE:
 * - Interfaccia → disaccoppiamento dall'implementazione
 * - Usa Domain Model (User), NON Entity
 */
public interface UserDao {

    void save(User user);

    Optional<User> findByEmail(String email);
}
