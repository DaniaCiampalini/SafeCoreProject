package com.safecore.business.service;

public interface BackupService {

    void exportBackup(byte[] encryptedData);

    byte[] importBackup(byte[] backupData);
}
