package com.safecore;

import com.safecore.business.domain.PasswordEntry;
import com.safecore.business.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DatabaseDemoRunner implements CommandLineRunner {

    private final UserService userService;
    private final PasswordService passwordService;
    private final PasswordResetService resetService;

    // Dependency Injection automatica: niente più "new UserDaoJpa()"
    public DatabaseDemoRunner(UserService userService,
                              PasswordService passwordService,
                              PasswordResetService resetService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.resetService = resetService;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== SafeCore: Demo Operativa Spring Boot ===");

        final String EMAIL = "mario.rossi@safecore.it";
        final String PASS_INIZIALE = "MasterPassword2026!";

        try {
            // FASE 1: REGISTRAZIONE
            System.out.println("\n1. Test Registrazione...");
            try {
                userService.register(EMAIL, PASS_INIZIALE);
                System.out.println("   -> Utente registrato correttamente.");
            } catch (Exception e) {
                System.out.println("   -> Nota: " + e.getMessage());
            }

            // FASE 2: VAULT
            System.out.println("\n2. Test Vault Password...");
            if (userService.login(EMAIL, PASS_INIZIALE).isPresent()) {
                passwordService.addCredential("Amazon", "mario_shop", "prime-secret-123");
                passwordService.addCredential("Gmail", "m.rossi@gmail.com", "google-vault-password");

                visualizzaVault();
            }

            // FASE 3: RESET PASSWORD
            System.out.println("\n3. Test Flusso Reset Password...");
            String token = resetService.requestReset(EMAIL);
            String nuovaPass = "NuovaPasswordSicura2026!";

            resetService.resetPassword(EMAIL, token, nuovaPass);

            if (userService.login(EMAIL, nuovaPass).isPresent()) {
                System.out.println("   -> [SUCCESSO] Login effettuato con la nuova password.");
            }

        } catch (Exception e) {
            System.err.println("[ERRORE DEMO] " + e.getMessage());
        }
    }

    private void visualizzaVault() {
        System.out.println("--- VAULT CONTENUTO ---");
        List<PasswordEntry> entries = passwordService.getAllEntries();
        for (PasswordEntry entry : entries) {
            System.out.printf("Service: %-10s | User: %-20s | Password: %s%n",
                    entry.getServiceName(),
                    entry.getUsername(),
                    passwordService.getDecryptedPassword(entry));
        }
    }
}