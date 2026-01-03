package com.safecore.persistence.dao;

import com.safecore.model.PasswordEntry;
import com.safecore.persistence.entity.PasswordEntryEntity;

public class PasswordMapper {

    public static PasswordEntryEntity toEntity(PasswordEntry domain) {
        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setId(domain.getId());
        entity.setServiceName(domain.getServiceName());
        entity.setUsername(domain.getUsername());
        entity.setEncryptedPassword(domain.getEncryptedPassword());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    public static PasswordEntry toDomain(PasswordEntryEntity entity) {
        return new PasswordEntry.Builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .username(entity.getUsername())
                .encryptedPassword(entity.getEncryptedPassword())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}