package com.safecore.business.service;

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

        // Jackson ci serve per convertire gli oggetti in JSON (comodo per l'export)
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Metodi per l'Observer Pattern: chi vuole essere avvisato dei cambiamenti si registra qui
    public void addObserver(VaultObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(VaultObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        observers.forEach(VaultObserver::onVaultChanged);
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
        // Recuperiamo l'utente che sta facendo l'operazione
        String currentUserEmail = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato in sessione"));

        // Momento cruciale: cifriamo la password prima di toccare il DB.
        // Zero-Knowledge: noi non salviamo MAI la password in chiaro.
        byte[] encryptedData = encryptionStrategy.encrypt(plainPassword);

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(serviceName);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setUser(user);
        entity.setExpiresAt(expiresAt);

        passwordEntryRepository.save(entity);
        
        // Avvisiamo tutti quelli che stanno guardando il vault (es. la Dashboard)
        // che i dati sono cambiati, così si aggiornano da soli.
        notifyObservers();
    }

    /**
     * Rimuove le password scadute.
     */
    @Transactional
    public void cleanupExpiredEntries() {
        passwordEntryRepository.deleteByExpiresAtBefore(java.time.LocalDateTime.now());
        notifyObservers();
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
        notifyObservers();
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