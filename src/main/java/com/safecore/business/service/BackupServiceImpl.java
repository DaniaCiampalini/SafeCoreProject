package com.safecore.business.service;

import com.safecore.security.hamming.ErrorCorrectionCodec;
import com.safecore.security.hamming.Hamming74Codec;

/**
 * Gestisce i backup applicando l'algoritmo di Hamming.
 * L'idea è: non solo esportiamo i dati, ma aggiungiamo dei bit di controllo
 * così se il file si corrompe leggermente, possiamo ripararlo al volo!
 */
public class BackupServiceImpl implements BackupService {

    private final ErrorCorrectionCodec codec = new Hamming74Codec();

    @Override
    public void exportBackup(byte[] encryptedData) {
        // Codifica con Hamming PRIMA di "salvare"
        byte[] protectedData = codec.encode(encryptedData);
        System.out.println("Backup generato con successo. Dimensione protetta: " + protectedData.length + " bytes.");
        // Qui salveresti protectedData su file
    }

    @Override
    public byte[] importBackup(byte[] backupData) {
        // backupData dovrebbe essere già codificato con Hamming
        // Quindi decodifichiamo (e correggiamo errori)
        return codec.decode(backupData);
    }
}