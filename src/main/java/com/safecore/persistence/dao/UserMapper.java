package com.safecore.persistence.dao;

import com.safecore.business.domain.User;
import com.safecore.business.domain.UserBuilder;
import com.safecore.persistence.entity.UserEntity;

/**
 * Mapper esplicito Domain ↔ Entity.
 *
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
        return new UserBuilder()
                .id(entity.getId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .mfaEnabled(entity.isMfaEnabled())
                .build();
    }
}
