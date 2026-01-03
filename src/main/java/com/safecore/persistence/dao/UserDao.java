package com.safecore.persistence.dao;

import com.safecore.business.domain.User;
import java.util.Optional;

/**
 * Questa interfaccia è il nostro contratto per i dati degli utenti.
 * Ho scelto di usare un'interfaccia così se un giorno decidiamo di mollare JPA
 * e passare a un altro DB, ci basta creare una nuova implementazione senza
 * toccare una riga dei nostri Service.
 * Nota che qui usiamo 'User' (il nostro oggetto di business), non le Entity del DB.
 */
public interface UserDao {

    void save(User user);

    Optional<User> findByEmail(String email);

    // lo usiamo nel Service per velocizzare i controlli in registrazione
    boolean existsByEmail(String email);

    void updatePassword(String email, String hashedPassword);
}