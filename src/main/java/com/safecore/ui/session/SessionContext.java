package com.safecore.ui.session;

/**
 * Contesto di sessione dell'applicazione.
 * Responsabilità:
 * - Memorizzare lo stato dell'utente loggato
 * - Rendere accessibili info di sessione ai controller
 * Scelte SE:
 * - Singleton applicativo
 * - Stato centralizzato
 * - Nessuna dipendenza da UI o persistence
 */
public final class SessionContext {

    private static String loggedUserEmail;

    private SessionContext() {
    }

    public static void login(String email) {
        loggedUserEmail = email;
    }

    public static void logout() {
        loggedUserEmail = null;
    }

    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }

    public static String getLoggedUserEmail() {
        return loggedUserEmail;
    }
}
