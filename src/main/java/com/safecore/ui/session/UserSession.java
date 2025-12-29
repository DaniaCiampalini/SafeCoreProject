package com.safecore.ui.session;

/**
 * Gestione minimale della sessione utente.
 *
 * - Tiene traccia dell'utente autenticato
 * - Stateless verso DB
 * - Facile da estendere
 */
public final class UserSession {

    private static String loggedUserEmail;

    private UserSession() {}

    public static void login(String email) {
        loggedUserEmail = email;
    }

    public static void logout() {
        loggedUserEmail = null;
    }

    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }

    public static String getUserEmail() {
        return loggedUserEmail;
    }
}
