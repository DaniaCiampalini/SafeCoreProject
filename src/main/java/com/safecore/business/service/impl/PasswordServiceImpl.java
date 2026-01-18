package com.safecore.business.service.impl;

import com.safecore.business.domain.PasswordEntry;
import com.safecore.business.service.PasswordService;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.security.EncryptionStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementazione concreta della gestione password.
 * Si occupa di salvare, recuperare e cancellare le password cifrate.
 * Usa una strategia di cifratura iniettata per mantenere il codice flessibile.
 * Utilizza transazioni per garantire la coerenza dei dati.
 */

@Service
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEntryRepository passwordRepository;
    private final EncryptionStrategy encryption;

    public PasswordServiceImpl(PasswordEntryRepository passwordRepository, EncryptionStrategy encryption) {
        this.passwordRepository = passwordRepository;
        this.encryption = encryption;
    }

    @Override
    @Transactional
    public void addCredential(String service, String username, String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("La password non può essere vuota");
        }

        byte[] encryptedData = encryption.encrypt(plainPassword);

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(service);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setCreatedAt(LocalDateTime.now());

        passwordRepository.save(entity);
    }

    // Decifra i byte salvati per far rivedere la password all'utente.
    @Override
    public String getDecryptedPassword(PasswordEntry domainEntry) {
        return encryption.decrypt(domainEntry.getEncryptedPassword());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasswordEntry> getAllEntries() {
        return passwordRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEntry(UUID id) {
        passwordRepository.deleteById(id);
    }

    /**
     * Helper per mappare l'entità di persistenza in oggetto di dominio.
     * Mantiene il codice pulito e separato.
     */
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