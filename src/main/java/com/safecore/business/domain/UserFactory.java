package com.safecore.business.domain;

import java.util.UUID;

/**
 * La Factory per gli utenti.
 * Mentre il Builder serve a costruire l'oggetto passo dopo passo,
 * la Factory è comoda quando abbiamo già tutti i pezzi e vogliamo
 * creare l'oggetto "al volo" in un colpo solo.
 */
public final class UserFactory {

    private UserFactory() {
        // Classe utility: niente istanze
    }

    /**
     * Crea un oggetto User completo.
     */
    public static User createNew(
            UUID id,
            String email,
            String passwordHash
    ) {
        return new User(id, email, passwordHash);
    }
}
