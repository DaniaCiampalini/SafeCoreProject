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

        // Qui usiamo il Factory Pattern: chiediamo alla factory la strategia di default (AES)
        // Se domani vogliamo cambiare algoritmo, ci basta toccare la factory.
        this.encryptionStrategy = encryptionFactory.getDefaultStrategy();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void addObserver(VaultObserver observer) { observers.add(observer); }
    public void removeObserver(VaultObserver observer) { observers.remove(observer); }
    private void notifyObservers() { observers.forEach(VaultObserver::onVaultChanged); }

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

        // Avvisiamo tutti quelli che stanno guardando il vault (es. la Dashboard)
        // che i dati sono cambiati, così si aggiornano da soli.
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

    /**
     * Elimina una voce dal vault.
     */
    @Transactional
    public void deleteEntry(UUID id) {
        passwordEntryRepository.deleteById(id);
        notifyObservers();
    }

    @Transactional(readOnly = true)
    public void exportVaultAsEncryptedJson(File destinationFile) throws Exception {
        List<PasswordEntryEntity> entries = getEntriesForCurrentUser();
        if (entries.isEmpty()) throw new RuntimeException("Vault vuoto.");

        // Il JSON ora non conterrà l'utente grazie a @JsonIgnore nell'Entity
        String jsonContent = objectMapper.writeValueAsString(entries);
        byte[] encryptedPackage = encryptionStrategy.encrypt(jsonContent);
        String finalBase64 = Base64.getEncoder().encodeToString(encryptedPackage);

        Files.writeString(destinationFile.toPath(), finalBase64, StandardCharsets.UTF_8);
    }

    @Transactional
    public void importVaultFromEncryptedJson(File sourceFile) throws Exception {
        String base64 = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
        byte[] encrypted = Base64.getDecoder().decode(base64.trim());
        String json = encryptionStrategy.decrypt(encrypted);

        List<PasswordEntryEntity> imported = objectMapper.readValue(json, new TypeReference<>() {});

        for (PasswordEntryEntity entry : imported) {
            // Decifra con vecchia chiave, ricifra con nuova tramite addEntry
            String plain = decryptPassword(entry.getEncryptedPassword());
            addEntry(entry.getServiceName(), entry.getUsername(), plain, entry.getExpiresAt());
        }
    }
}