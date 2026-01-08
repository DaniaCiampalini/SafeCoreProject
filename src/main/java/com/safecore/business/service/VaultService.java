package com.safecore.business.service;

import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.PasswordEntryRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.EncryptionStrategy;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private final PasswordEntryRepository passwordEntryRepository;
    private final UserRepository userRepository;
    private final EncryptionStrategy encryptionStrategy;

    public VaultService(PasswordEntryRepository passwordEntryRepository,
                        UserRepository userRepository,
                        EncryptionStrategy encryptionStrategy) {
        this.passwordEntryRepository = passwordEntryRepository;
        this.userRepository = userRepository;
        this.encryptionStrategy = encryptionStrategy;
    }

    /**
     * Salva una nuova password nel Vault cifrandola.
     */
    @Transactional
    public void addEntry(String serviceName, String username, String plainPassword) {
        String currentUserEmail = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Utente non trovato in sessione"));

        // Cifratura tramite AES
        byte[] encryptedData = encryptionStrategy.encrypt(plainPassword);

        PasswordEntryEntity entity = new PasswordEntryEntity();
        entity.setServiceName(serviceName);
        entity.setUsername(username);
        entity.setEncryptedPassword(encryptedData);
        entity.setUser(user);

        passwordEntryRepository.save(entity);
    }

    /**
     * Recupera tutte le password dell'utente loggato.
     * Restituisce le entità (ancora cifrate).
     */
    public List<PasswordEntryEntity> getEntriesForCurrentUser() {
        String email = SessionContext.getCurrentUserEmail();
        return passwordEntryRepository.findByUserEmail(email);
    }

    /**
     * Decifra una password specifica quando l'utente preme "Mostra".
     */
    public String decryptPassword(byte[] encryptedPassword) {
        return encryptionStrategy.decrypt(encryptedPassword);
    }

    /**
     * Elimina una voce dal vault.
     */
    @Transactional
    public void deleteEntry(java.util.UUID id) {
        passwordEntryRepository.deleteById(id);
    }
}