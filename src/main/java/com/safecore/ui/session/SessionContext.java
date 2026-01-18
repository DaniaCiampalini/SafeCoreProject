package com.safecore.ui.session;

import java.time.LocalDateTime;


/**
 * Tiene traccia di chi è loggato nell'applicazione desktop.
 * Usata dai controller per sapere l'utente "corrente".
 */
public final class SessionContext {

    // Variabili statiche per tenere traccia dell'utente loggato
    private static volatile String loggedUserEmail;
    private static LocalDateTime loginTime;

    private SessionContext() {
        // Classe utility: non si creano oggetti SessionContext
    }

    /**
     * Imposta l'utente loggato.
     */
    public static void login(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email di sessione non valida");
        }
        loggedUserEmail = email;
        loginTime = LocalDateTime.now();
        System.out.println("Benvenuto! Sessione avviata per: " + email);
    }


    public static void logout() {
        loggedUserEmail = null;
        loginTime = null;
    }


    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }

    /**
     * Restituisce l'email dell'utente loggato.
     * Lancia un'eccezione se nessun utente è loggato.
     */
    public static String getCurrentUserEmail() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Nessun utente loggato. Accesso negato.");
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