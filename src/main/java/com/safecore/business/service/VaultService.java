package com.safecore.business.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionFactory;
import com.safecore.security.EncryptionStrategy;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per gestire il vault delle password.
 * Fornisce metodi per aggiungere, eliminare, esportare e importare voci del vault.
 * Utilizza una strategia di crittografia per proteggere le password memorizzate.
 */

@Service
public class VaultService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionStrategy encryptionStrategy;
    private final ObjectMapper objectMapper;
    private final List<VaultObserver> observers = new ArrayList<>();

    public VaultService(PasswordEntryRepository passwordEntryRepository,
                        UserRepository userRepository,
                        EncryptionFactory encryptionFactory) {
        this.passwordEntryRepository = passwordEntryRepository;
        this.userRepository = userRepository;

        // Qui usiamo il Factory Pattern per ottenere la strategia di crittografia desiderata.
        this.encryptionStrategy = encryptionFactory.getDefaultStrategy();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void addObserver(VaultObserver observer) { observers.add(observer); }
    public void removeObserver(VaultObserver observer) { observers.remove(observer); }
    public void notifyObservers() { observers.forEach(VaultObserver::onVaultChanged); }

    @Transactional
    public void addEntry(String service, String username, String plain, LocalDateTime expiry) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato: " + email));

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(service);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptionStrategy.encrypt(plain));
        entity.setUser(user);
        entity.setExpiresAt(expiry);

        passwordEntryRepository.save(entity);

        // Notifichiamo gli osservatori che il vault è cambiato così possono aggiornare l'interfaccia utente.
        notifyObservers();
    }

    public void addEntry(String service, String username, String plain) {
        addEntry(service, username, plain, null);
    }

    @Transactional
    public void cleanupExpiredEntries() {
        passwordEntryRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        notifyObservers();
    }

    /**
     * Recupera tutte le password dell'utente loggato.
     */
    public List<PasswordEntryEntity> getEntriesForCurrentUser() {
        return passwordEntryRepository.findByUserEmail(SessionContext.getCurrentUserEmail());
    }

    public String decryptPassword(byte[] encrypted) {
        return (encrypted == null) ? "" : encryptionStrategy.decrypt(encrypted);
    }


    @Transactional
    public void deleteEntry(UUID id) {
        passwordEntryRepository.deleteById(id);
        notifyObservers();
    }

    @Transactional(readOnly = true)
    public void exportVaultAsEncryptedJson(File destinationFile) throws Exception {
        List<PasswordEntryEntity> entries = getEntriesForCurrentUser();
        if (entries.isEmpty()) throw new RuntimeException("Vault vuoto.");

        // Creiamo una lista di DTO o mappe che contengono la password in chiaro
        // in modo che il pacchetto cifrato finale contenga dati leggibili dopo la decifratura
        List<java.util.Map<String, Object>> exportList = new ArrayList<>();
        for (PasswordEntryEntity e : entries) {
            exportList.add(java.util.Map.of(
                    "service", e.getServiceName(),
                    "username", e.getUsername(),
                    "plainPassword", decryptPassword(e.getEncryptedPassword()), // Decifriamo
                    "expiry", e.getExpiresAt() != null ? e.getExpiresAt().toString() : ""
            ));
        }

        String jsonContent = objectMapper.writeValueAsString(exportList);

        byte[] encryptedPackage = encryptionStrategy.encrypt(jsonContent);
        String finalBase64 = Base64.getEncoder().encodeToString(encryptedPackage);

        Files.writeString(destinationFile.toPath(), finalBase64, StandardCharsets.UTF_8);
    }

    @Transactional
    public void importVaultFromEncryptedJson(File sourceFile) throws Exception {
        String base64 = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        byte[] encrypted = Base64.getDecoder().decode(base64.trim());
        String json = encryptionStrategy.decrypt(encrypted);

        // Usiamo una lista di mappe per leggere il formato creato sopra
        List<java.util.Map<String, Object>> imported = objectMapper.readValue(json, new TypeReference<>() {});

        for (java.util.Map<String, Object> data : imported) {
            String service = (String) data.get("service");
            String user = (String) data.get("username");
            String plain = (String) data.get("plainPassword");
            String expiryStr = (String) data.get("expiry");

            LocalDateTime expiry = (expiryStr != null && !expiryStr.isEmpty())
                    ? LocalDateTime.parse(expiryStr) : null;

            addEntry(service, user, plain, expiry);  // provvederà a cifrare con la chiave attuale del sistema
        }
    }
}