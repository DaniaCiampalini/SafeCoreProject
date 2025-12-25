package com.safecore.business.service;

import com.safecore.security.hamming.ErrorCorrectionCodec;
import com.safecore.security.hamming.Hamming74Codec;

/**
 * Gestisce esportazione e importazione dei backup.
 * Applica Hamming come controllo di integrità.
 */
public class BackupServiceImpl implements BackupService {

    private final ErrorCorrectionCodec codec = new Hamming74Codec();

    @Override
    public void exportBackup(byte[] encryptedData) {
        byte[] protectedData = codec.encode(encryptedData);
        // TODO: scrivere su file
    }

    @Override
    public byte[] importBackup(byte[] backupData) {
        return codec.decode(backupData);
    }
}
