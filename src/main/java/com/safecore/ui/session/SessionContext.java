package com.safecore.ui.session;

import java.time.LocalDateTime;

/**
 * Contesto di sessione centralizzato (Singleton).
 * Unifica UserSession e SessionContext per eliminare ridondanze.
 * * Scelte di Ingegneria del Software:
 * - Thread-safe: usiamo volatile per garantire visibilità tra i thread JavaFX.
 * - Tracciamento: aggiunta l'ora del login per scopi di auditing.
 */
public final class SessionContext {

    // volatile garantisce che il valore sia aggiornato correttamente tra i thread
    private static volatile String loggedUserEmail;
    private static LocalDateTime loginTime;

    private SessionContext() {
        // Impedisce l'istanziazione esterna
    }

    /**
     * Inizializza la sessione utente.
     */
    public static void login(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email di sessione non valida");
        }
        loggedUserEmail = email;
        loginTime = LocalDateTime.now();
    }

    /**
     * Termina la sessione e pulisce i dati sensibili.
     */
    public static void logout() {
        loggedUserEmail = null;
        loginTime = null;
    }

    /**
     * Verifica se esiste una sessione attiva.
     */
    public static boolean isLoggedIn() {
        return loggedUserEmail != null;
    }

    /**
     * Restituisce l'email dell'utente corrente.
     */
    public static String getLoggedUserEmail() {
        return loggedUserEmail;
    }

    /**
     * Restituisce l'orario del login (utile per la Dashboard).
     */
    public static LocalDateTime getLoginTime() {
        return loginTime;
    }
}