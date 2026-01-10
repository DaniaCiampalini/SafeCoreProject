package com.safecore.business.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionStrategy;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class VaultService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionStrategy encryptionStrategy;
    private final ObjectMapper objectMapper;

    public VaultService(PasswordEntryRepository passwordEntryRepository,
                        UserRepository userRepository,
                        EncryptionStrategy encryptionStrategy) {
        this.passwordEntryRepository = passwordEntryRepository;
        this.userRepository = userRepository;
        this.encryptionStrategy = encryptionStrategy;

        // Inizializziamo Jackson per la gestione JSON
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Salva una nuova password nel Vault cifrandola.
     */
    @Transactional
    public void addEntry(String serviceName, String username, String plainPassword) {
        addEntry(serviceName, username, plainPassword, null);
    }

    @Transactional
    public void addEntry(String serviceName, String username, String plainPassword, java.time.LocalDateTime expiresAt) {
        String currentUserEmail = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato in sessione"));

        // Cifratura tramite la strategia configurata (es. AES)
        byte[] encryptedData = encryptionStrategy.encrypt(plainPassword);

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(serviceName);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setUser(user);
        entity.setExpiresAt(expiresAt);

        passwordEntryRepository.save(entity);
    }

    /**
     * Rimuove le password scadute.
     */
    @Transactional
    public void cleanupExpiredEntries() {
        passwordEntryRepository.deleteByExpiresAtBefore(java.time.LocalDateTime.now());
    }

    /**
     * Recupera tutte le password dell'utente loggato.
     */
    public List<PasswordEntryEntity> getEntriesForCurrentUser() {
        String email = SessionContext.getCurrentUserEmail();
        if (email == null) return List.of();
        return passwordEntryRepository.findByUserEmail(email);
    }

    /**
     * Decifra una password specifica.
     */
    public String decryptPassword(byte[] encryptedPassword) {
        if (encryptedPassword == null) return "";
        return encryptionStrategy.decrypt(encryptedPassword);
    }

    /**
     * Elimina una voce dal vault.
     */
    @Transactional
    public void deleteEntry(UUID id) {
        passwordEntryRepository.deleteById(id);
    }

    /**
     * Esporta l'intero Vault in un file JSON cifrato.
     * Il file conterrà i dati originali (ancora cifrati individualmente)
     * e l'intero pacchetto sarà cifrato ulteriormente per sicurezza.
     */
    public void exportVaultAsEncryptedJson(File destinationFile) throws Exception {
        // 1. Recupera le voci dell'utente
        List<PasswordEntryEntity> entries = getEntriesForCurrentUser();

        if (entries.isEmpty()) {
            throw new RuntimeException("Il vault è vuoto. Nulla da esportare.");
        }

        // 2. Converte la lista in una stringa JSON
        // Nota: escludiamo i dati sensibili dell'oggetto User per non portarceli nel backup
        String jsonContent = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(entries);

        // 3. Cifra l'intero JSON
        // Trasformiamo il JSON in byte e usiamo la strategia di cifratura esistente
        byte[] encryptedBackupBytes = encryptionStrategy.encrypt(jsonContent);

        // 4. Codifica in Base64 per rendere il file leggibile come testo ma protetto
        String finalContent = Base64.getEncoder().encodeToString(encryptedBackupBytes);

        // 5. Scrittura su disco
        Files.writeString(destinationFile.toPath(), finalContent, StandardCharsets.UTF_8);
    }
}