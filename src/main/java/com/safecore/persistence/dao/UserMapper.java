package com.safecore.persistence.dao;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.persistence.entity.UserEntity;

/**
 * Questa classe fa da ponte tra il database e il nostro codice.
 * Non vogliamo che il database "contamini" il nostro Domain Model.
 * Se cambiamo un nome colonna sul DB, cambiamo solo qui e il resto del programma non se ne accorge nemmeno.
 * Mapper esplicito Domain ↔ Entity.
 * Scelta progettuale:
 * - Evita accoppiamento diretto
 * - Controllo completo sulla trasformazione
 * - Facilita refactoring futuri
 */
public class UserMapper {

    public static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setMfaEnabled(user.isMfaEnabled());
        return entity;
    }

    public static User toDomain(UserEntity entity) {
        // Usiamo il Builder per ricostruire l'oggetto User in modo sicuro e pulito
        return new UserBuilder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .mfaEnabled(entity.isMfaEnabled())
                .build();
    }
}
