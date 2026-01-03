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
        // Applichiamo Hamming per la protezione
        byte[] protectedData = codec.encode(encryptedData);

        // Risolto il warning: simuliamo l'output o logghiamo la dimensione
        System.out.println("Backup generato con successo. Dimensione protetta: " + protectedData.length + " bytes.");
        // TODO: In una fase successiva, useremo un FileOutputStream qui.
    }

    @Override
    public byte[] importBackup(byte[] backupData) {
        // Qui Hamming prova a correggere eventuali errori prima di restituire i dati
        return codec.decode(backupData);
    }
}