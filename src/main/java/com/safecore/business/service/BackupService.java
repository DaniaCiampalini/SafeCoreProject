package com.safecore.business.service;

import java.io.File;
import java.io.IOException;

public interface BackupService {
    /**
     * Coordina l'esportazione sicura del vault su un file fisico.
     * @param targetFile Il file di destinazione selezionato dall'utente.
     */
    void exportBackup(File targetFile) throws IOException;

    /**
     * Coordina l'importazione e il ripristino delle entry da un file.
     * @param sourceFile Il file .safecore da caricare.
     */
    void importBackup(File sourceFile) throws IOException;
}