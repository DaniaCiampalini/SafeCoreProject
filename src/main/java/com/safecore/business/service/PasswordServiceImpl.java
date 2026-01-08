package com.safecore.business.service;

import com.safecore.business.domain.PasswordEntry;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.security.AESEncryptionStrategy;
import com.safecore.security.EncryptionStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service // Gestito da Spring
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEntryRepository passwordRepository;
    private final EncryptionStrategy encryption = new AESEncryptionStrategy();

    public PasswordServiceImpl(PasswordEntryRepository passwordRepository) {
        this.passwordRepository = passwordRepository;
    }

    @Override
    @Transactional
    public void addCredential(String service, String username, String plainPassword) {
        // 1. Cifratura tramite Strategy
        byte[] encryptedData = encryption.encrypt(plainPassword);

        // 2. Creazione Entity (per il DB)
        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(service);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setCreatedAt(LocalDateTime.now());

        // 3. Salvataggio tramite Repository
        passwordRepository.save(entity);
    }

    @Override
    public String getDecryptedPassword(PasswordEntry domainEntry) {
        return encryption.decrypt(domainEntry.getEncryptedPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasswordEntry> getAllEntries() {
        // Recupera le entity e le mappa nel Domain Model per la UI
        return passwordRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEntry(UUID id) {
        passwordRepository.deleteById(id);
    }

    // Mapper interno: trasforma Entity DB in Domain Object pulito
    private PasswordEntry mapToDomain(PasswordEntryEntity entity) {
        return new PasswordEntry.Builder()
                .id(entity.getId())
                .serviceName(entity.getServiceName())
                .username(entity.getUsername())
                .encryptedPassword(entity.getEncryptedPassword())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}