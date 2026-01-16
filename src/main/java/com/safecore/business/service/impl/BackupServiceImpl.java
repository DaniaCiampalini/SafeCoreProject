package com.safecore.business.service.impl;

import com.safecore.business.service.BackupService;
import com.safecore.business.service.VaultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupServiceImpl.class);
    private final VaultService vaultService;

    public BackupServiceImpl(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    public void exportBackup(File targetFile) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Il file di destinazione non può essere nullo.");
        }

        try {
            logger.info("Avvio esportazione backup nel file: {}", targetFile.getName());

            // Deleghiamo al VaultService la trasformazione dei dati in formato protetto
            vaultService.exportVaultAsEncryptedJson(targetFile);

            logger.info("Backup completato con successo.");
        } catch (Exception e) {
            logger.error("Errore durante l'esportazione: ", e);
            throw new IOException("Impossibile completare l'esportazione: " + e.getMessage(), e);
        }
    }

    @Override
    public void importBackup(File sourceFile) throws IOException {
        if (sourceFile == null || !sourceFile.exists()) {
            throw new IOException("File di origine non valido o inesistente.");
        }

        try {
            logger.info("Avvio importazione backup dal file: {}", sourceFile.getName());

            // Deleghiamo al VaultService il parsing, la decifratura e il salvataggio nel DB
            vaultService.importVaultFromEncryptedJson(sourceFile);

            logger.info("Importazione completata con successo.");
        } catch (Exception e) {
            logger.error("Errore durante l'importazione: ", e);
            throw new IOException("Impossibile ripristinare il backup. Il file potrebbe essere corrotto o la chiave errata.", e);
        }
    }
}