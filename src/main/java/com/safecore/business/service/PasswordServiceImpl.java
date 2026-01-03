package com.safecore.business.service;

import com.safecore.model.PasswordEntry;
import com.safecore.persistence.dao.PasswordEntryDao;
import com.safecore.security.AESEncryptionStrategy;
import com.safecore.security.EncryptionStrategy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Questa classe è il Vault delle password.
 * * Scelte di Sicurezza:
 * - Le password non toccano mai il DB in chiaro (usiamo AESEncryptionStrategy).
 * - Usiamo UUID random per ogni voce, così sono inattaccabili.
 * - Usiamo encrypt() per salvare, decrypt() per leggere.
 */
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEntryDao passwordDao;
    private final EncryptionStrategy encryption = new AESEncryptionStrategy();

    /**
     * Il DAO viene iniettato così possiamo cambiare database
     * senza rompere la logica di cifratura.
     */
    public PasswordServiceImpl(PasswordEntryDao passwordDao) {
        this.passwordDao = passwordDao;
    }

    @Override
    public void addCredential(String service, String username, String plainPassword) {
        // 1. Cifratura: Trasformiamo la stringa leggibile in byte protetti
        byte[] encryptedData = encryption.encrypt(plainPassword);

        // 2. Creazione Entry tramite Builder (Immutabile)
        PasswordEntry entry = new PasswordEntry.Builder()
                .id(UUID.randomUUID())
                .serviceName(service)
                .username(username)
                .encryptedPassword(encryptedData)
                .createdAt(LocalDateTime.now())
                .build();

        // 3. Persistenza
        passwordDao.save(entry);
    }

    @Override
    public String getDecryptedPassword(PasswordEntry entry) {
        /**
         * RISOLTO WARNING: Qui usiamo finalmente il metodo decrypt().
         * Prende i byte dal database e li riporta in chiaro solo per la UI.
         */
        return encryption.decrypt(entry.getEncryptedPassword());
    }

    @Override
    public List<PasswordEntry> getAllEntries() {
        // Recupera la lista di tutte le credenziali (ancora cifrate)
        return passwordDao.findAll();
    }

    @Override
    public void deleteEntry(UUID id) {
        passwordDao.deleteById(id);
    }
}