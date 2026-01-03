package com.safecore;

import com.safecore.business.service.*;
import com.safecore.persistence.dao.*;
import com.safecore.persistence.util.JpaUtil;

/**
 * Punto di ingresso dell'applicazione SafeCore.
 * Qui assembliamo i vari componenti (Dependency Injection manuale).
 */
public class Main {

    public static void main(String[] args) {
        // 1. Inizializziamo il Layer di Persistenza (DAO)
        // Questi oggetti sanno come parlare con il DB tramite JPA
        UserDao userDao = new UserDaoJpa();
        PasswordEntryDao passwordDao = new PasswordEntryDaoJpa();
        PasswordResetTokenDao tokenDao = new PasswordResetTokenDaoJpa();

        // 2. Inizializziamo il Layer di Business (Service)
        // Passiamo i DAO ai relativi Service.
        // Nota come i Service non sappiano nulla di JPA, vedono solo le interfacce!
        UserService userService = new UserServiceImpl(userDao);
        PasswordService passwordService = new PasswordServiceImpl(passwordDao);
        PasswordResetService resetService = new PasswordResetServiceImpl(userDao, tokenDao);

        // 3. Setup dello Shutdown Hook
        // RISOLTO: Qui usiamo il metodo JpaUtil.close() che prima era "unused".
        // Garantisce che se l'utente chiude l'app, il database venga rilasciato correttamente.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Spegnimento in corso...");
            JpaUtil.close();
        }));

        System.out.println("SafeCore avviato correttamente.");

        // --- ESEMPIO DI UTILIZZO ---
        // In una vera app, qui lanceresti la tua interfaccia JavaFX:
        // Application.launch(SafeCoreGui.class, args);

        // Per testare velocemente se tutto gira:
        testIniziale(userService, passwordService);
    }

    private static void testIniziale(UserService userService, PasswordService passwordService) {
        try {
            System.out.println("Test: Registrazione utente...");
            // Registriamo un utente di prova
            // userService.register("test@safecore.com", "PasswordSicura123!");

            System.out.println("Test: Aggiunta password nel vault...");
            // passwordService.addCredential("Gmail", "mario.rossi", "segreto123");

        } catch (Exception e) {
            System.err.println("Errore durante il test: " + e.getMessage());
        }
    }
}