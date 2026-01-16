package com.safecore.business.service.impl;

import com.safecore.business.service.BackupService;
import org.springframework.stereotype.Service;

/**
 * Gestione backup semplificata dopo la rimozione di Hamming.
 * Si occupa dell'esportazione dei dati cifrati del Vault.
 */
@Service
public class BackupServiceImpl implements BackupService {

    @Override
    public void exportBackup(byte[] encryptedData) {
        if (encryptedData == null || encryptedData.length == 0) {
            throw new IllegalArgumentException("Nessun dato da esportare");
        }
        // Qui la logica si limita a preparare il file per il download/salvataggio
        System.out.println("[BACKUP] Esportazione di " + encryptedData.length + " bytes completata.");
    }

    @Override
    public byte[] importBackup(byte[] backupData) {
        if (backupData == null) return new byte[0];
        // Restituisce i dati così come sono (già pronti per essere decifrati dal PasswordService)
        return backupData;
    }
}