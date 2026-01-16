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
 * È qui che succede la magia della cifratura prima di salvare sul database.
 * Usiamo il concetto di "Zero Knowledge": il server (o il DB) vede solo byte cifrati.
 */
@Service
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEntryRepository passwordRepository;
    private final EncryptionStrategy encryption;

    public PasswordServiceImpl(PasswordEntryRepository passwordRepository, EncryptionStrategy encryption) {
        this.passwordRepository = passwordRepository;
        this.encryption = encryption;
    }

    /**
     * Prende una password in chiaro, la trasforma in un ammasso di byte illeggibili e la salva.
     */
    @Override
    @Transactional
    public void addCredential(String service, String username, String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("La password non può essere vuota");
        }

        // Cifriamo usando la strategia configurata (es. AES)
        byte[] encryptedData = encryption.encrypt(plainPassword);

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(service);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setCreatedAt(LocalDateTime.now());

        passwordRepository.save(entity);
    }

    /**
     * Decifra i byte salvati per far rivedere la password all'utente.
     */
    @Override
    public String getDecryptedPassword(PasswordEntry domainEntry) {
        return encryption.decrypt(domainEntry.getEncryptedPassword());
    }

    /**
     * Recupera tutto dal DB e lo trasforma in oggetti di dominio (puliti e pronti per l'uso).
     */
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
     * Helper per convertire l'Entity (roba da DB) in Domain Object (roba da logica).
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