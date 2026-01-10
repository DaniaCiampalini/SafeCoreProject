package com.safecore.ui.session;

import java.time.LocalDateTime;

/**
 * Questa classe è come il "passaporto" dell'utente. 
 * È un Singleton (centralizzato) che si ricorda chi è loggato in questo momento.
 * In questo modo, qualsiasi parte dell'app (Service, Controller, ecc.) può sapere
 * chi sta facendo cosa senza dover passare l'utente avanti e indietro come un pallone.
 */
public final class SessionContext {

    // L'email dell'utente loggato. Usiamo volatile per la visibilità tra i thread.
    private static volatile String loggedUserEmail;
    private static LocalDateTime loginTime;

    private SessionContext() {
        // Classe utility: non si creano oggetti SessionContext
    }

    /**
     * Chiamato dal LoginController quando l'utente azzecca la password.
     */
    public static void login(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email di sessione non valida");
        }
        loggedUserEmail = email;
        loginTime = LocalDateTime.now();
        System.out.println("Benvenuto! Sessione avviata per: " + email);
    }

    /**
     * Pulisce tutto: l'utente non è più loggato.
     */
    public static void logout() {
        loggedUserEmail = null;
        loginTime = null;
    }

    /**
     * Ci dice se c'è qualcuno "al volante" dell'app.
     */
    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }

    /**
     * Recupera l'email di chi è loggato. 
     * Se nessuno è loggato, lancia un errore: è un controllo di sicurezza in più.
     */
    public static String getCurrentUserEmail() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Alt! Nessun utente loggato. Accesso negato.");
        }
        return loggedUserEmail;
    }

    public static String getLoggedUserEmail() {
        return getCurrentUserEmail();
    }

    public static LocalDateTime getLoginTime() {
        return loginTime;
    }
}