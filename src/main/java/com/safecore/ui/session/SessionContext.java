package com.safecore.ui.session;

import java.time.LocalDateTime;

/**
 * Contesto di sessione centralizzato (Singleton).
 * Gestisce l'identità dell'utente loggato per tutto il ciclo di vita dell'app.
 */
public final class SessionContext {

    private static volatile String loggedUserEmail;
    private static LocalDateTime loginTime;

    private SessionContext() {
        // Impedisce l'istanziazione
    }

    /**
     * Inizializza la sessione utente al login.
     */
    public static void login(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email di sessione non valida");
        }
        loggedUserEmail = email;
        loginTime = LocalDateTime.now();
        System.out.println("Sessione avviata per: " + email + " alle " + loginTime);
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
     * Metodo richiesto dal VaultService per recuperare l'identità dell'utente.
     * Lancia un'eccezione se chiamato senza una sessione attiva (fail-safe).
     */
    public static String getCurrentUserEmail() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("Nessun utente loggato. Accesso al Vault negato.");
        }
        return loggedUserEmail;
    }

    /**
     * Alias per compatibilità con i controller precedenti (se usati).
     */
    public static String getLoggedUserEmail() {
        return getCurrentUserEmail();
    }

    /**
     * Restituisce l'orario del login (utile per mostrare "Sessione attiva da..." nella Dashboard).
     */
    public static LocalDateTime getLoginTime() {
        return loginTime;
    }
}