package com.safecore.business.service;

import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.security.KeyManager;
import com.safecore.ui.session.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.io.File;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BackupIntegrationTest {

    @Autowired private VaultService vaultService;
    @Autowired private BackupService backupService;
    @Autowired private UserRepository userRepository;
    @Autowired private KeyManager keyManager;

    private File tempFile;
    private final String EMAIL = "test@gmail.com";
    private final String PASSWORD = "TestPassword123!";
    private byte[] salt;

    @BeforeEach
    void setUp() {
        // Prepariamo l'utente nel DB di test
        salt = new byte[32];
        if (userRepository.findByEmail(EMAIL).isEmpty()) {
            UserEntity user = new UserEntity();
            user.setEmail(EMAIL);
            user.setPasswordHash("dummy_hash");
            user.setDerivationSalt(salt); // Salt richiesto per la cifratura
            userRepository.save(user);
        }

        // Inizializza KeyManager per permettere encryption/decryption
        keyManager.initialize(PASSWORD, salt);

        SessionContext.login(EMAIL);
        tempFile = new File("test_backup.safecore");
    }

    @Test
    void testBackupAndImport() throws Exception {
        // 1. Aggiunta
        vaultService.addEntry("Netflix", "user", "pass123");

        // 2. Export
        backupService.exportBackup(tempFile);
        assertTrue(tempFile.exists());

        // 3. Import
        backupService.importBackup(tempFile);

        // 4. Verifica (devono essercene 2 ora: l'originale + l'importata)
        assertEquals(2, vaultService.getEntriesForCurrentUser().size());

        Files.deleteIfExists(tempFile.toPath());
    }
}