package com.safecore;

import com.safecore.business.domain.User;
import com.safecore.business.service.UserService;
import com.safecore.business.service.VaultService;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.ui.session.SessionContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SafeCoreIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private VaultService vaultService;

    @Test
    void testFullWorkflow() {
        String email = "integration@test.com";
        String password = "SecurePassword123!";

        // 1. Registrazione
        User user = userService.register(email, password);
        assertNotNull(user);
        assertEquals(email, user.getEmail());

        // 2. Login
        Optional<User> loggedIn = userService.login(email, password);
        assertTrue(loggedIn.isPresent());

        // Simuliamo la sessione
        SessionContext.login(email);

        try {
            // 3. Aggiunta Segreto
            vaultService.addEntry("TestService", "testUser", "secret123");

            // 4. Verifica
            List<PasswordEntryEntity> entries = vaultService.getEntriesForCurrentUser();
            assertFalse(entries.isEmpty());
            assertEquals("TestService", entries.get(0).getServiceName());

            String decrypted = vaultService.decryptPassword(entries.get(0).getEncryptedPassword());
            assertEquals("secret123", decrypted);
        } finally {
            SessionContext.logout();
        }
    }
}
