package com.safecore;

import com.safecore.business.service.*;
import com.safecore.model.PasswordEntry;
import com.safecore.persistence.dao.*;
import com.safecore.persistence.util.JpaUtil;
import com.safecore.security.hamming.Hamming74Codec;

import java.util.List;
import java.util.Optional;

/**
 * Punto di ingresso dell'applicazione SafeCore.
 * Segue i principi di Clean Architecture separando l'inizializzazione dalla logica.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== SafeCore: Avvio Sistema ===");

        if (!inizializzaDatabase()) {
            System.err.println("[ERRORE FATALE] Arresto del sistema.");
            System.exit(1);
        }

        // 1. Dependency Injection (Manuale in questa fase, ma strutturata)
        UserDao userDao = new UserDaoJpa();
        PasswordEntryDao passwordDao = new PasswordEntryDaoJpa();
        PasswordResetTokenDao tokenDao = new PasswordResetTokenDaoJpa();

        UserService userService = new UserServiceImpl(userDao);
        PasswordService passwordService = new PasswordServiceImpl(passwordDao);
        PasswordResetService resetService = new PasswordResetServiceImpl(userDao, tokenDao);

        // 2. Registrazione Shutdown Hook per la pulizia delle risorse
        configuraSpegnimento();

        System.out.println("[OK] Servizi pronti.");
        System.out.println("--------------------------------------------");

        // 3. Esecuzione Test Operativo (Simulation Mode)
        eseguiDemo(userService, passwordService, resetService);
    }

    private static boolean inizializzaDatabase() {
        try {
            // Verifichiamo che la persistence unit sia caricata correttamente
            JpaUtil.getEntityManager(); 
            System.out.println("[OK] Database connesso e schema validato.");
            return true;
        } catch (Exception e) {
            System.err.println("[ERRORE] Connessione database fallita: " + e.getLocalizedMessage());
            return false;
        }
    }

    private static void configuraSpegnimento() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[SISTEMA] Spegnimento e chiusura connessioni...");
            JpaUtil.close();
            System.out.println("[OK] Risorse rilasciate correttamente.");
        }));
    }

    private static void eseguiDemo(UserService userService, PasswordService passwordService, PasswordResetService resetService) {
        final String EMAIL = "mario.rossi@safecore.it";
        final String PASS_INIZIALE = "MasterPassword2026!";
        
        try {
            // FASE 1: GESTIONE UTENTE (Idempotente)
            System.out.println("1. Gestione Profilo Utente...");
            try {
                userService.register(EMAIL, PASS_INIZIALE);
                System.out.println("   -> Nuova registrazione effettuata.");
            } catch (IllegalArgumentException e) {
                System.out.println("   -> Utente già presente, procedo con i test di vault.");
            }

            // FASE 2: AUTENTICAZIONE E VAULT
            System.out.println("\n2. Test Accesso Vault...");
            if (userService.login(EMAIL, PASS_INIZIALE).isPresent()) {
                System.out.println("   -> Login OK. Aggiunta credenziali di test...");

                passwordService.addCredential("Amazon", "mario_shop", "prime-secret-123");
                passwordService.addCredential("Gmail", "m.rossi@gmail.com", "google-vault-password");

                visualizzaCredenziali(passwordService);
            }

            // FASE 3: CICLO DI RESET PASSWORD
            System.out.println("\n3. Test Flusso di Recupero (Security Check)...");
            String token = resetService.requestReset(EMAIL);
            String nuovaPass = "NuovaPasswordSicura2026!";

            resetService.resetPassword(EMAIL, token, nuovaPass);
            System.out.println("   -> Reset completato. Verifica nuova password...");

            if (userService.login(EMAIL, nuovaPass).isPresent()) {
                System.out.println("   -> [SUCCESSO] La nuova password è attiva.");
            }

            // FASE 4: CORREZIONE ERRORI (Hamming)
            eseguiTestHamming();

        } catch (Exception e) {
            System.err.println("[ECCEZIONE DURANTE DEMO] " + e.getMessage());
        }
    }

    private static void visualizzaCredenziali(PasswordService service) {
        System.out.println("\n--- VAULT CONTENUTO (Dati Decifrati al Volo) ---");
        List<PasswordEntry> entries = service.getAllEntries();
        for (PasswordEntry entry : entries) {
            System.out.printf("Piattaforma: %-10s | User: %-20s | Password: %s%n",
                    entry.getServiceName(), entry.getUsername(), service.getDecryptedPassword(entry));
        }
        System.out.println("------------------------------------------------");
    }

    private static void eseguiTestHamming() {
        System.out.println("\n4. Test Integrità (Hamming 7,4 Codec)...");
        Hamming74Codec codec = new Hamming74Codec();
        String original = "SafeCore2026";
        
        byte[] encoded = codec.encode(original.getBytes());
        // Simuliamo un bit flip sul sesto byte per testare la correzione
        encoded[5] ^= 0x02; 
        
        byte[] corrected = codec.decode(encoded);
        String result = new String(corrected);
        
        System.out.println("   -> Messaggio originale: " + original);
        System.out.println("   -> Messaggio dopo correzione errore bit-flip: " + result);
        System.out.println("   -> Risultato: " + (original.equals(result) ? "CORRETTO" : "FALLITO"));
    }
}