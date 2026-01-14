package com.safecore.business.service;

import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Component;

/**
 * Observer che invalida la sessione quando viene completato un reset password.
 * Forza il logout dell'utente per garantire che effettui nuovamente il login con la nuova password.
 */
@Component
public class SessionLogoutObserver implements PasswordResetObserver {

    @Override
    public void onPasswordResetCompleted(PasswordResetCompletedEvent event) {
        if (event == null) {
            throw new NullPointerException("Event cannot be null");
        }

        // Invalida la sessione solo se l'utente corrente è quello che ha resettato la password
        if (SessionContext.isLoggedIn()) {
            String currentUser = SessionContext.getCurrentUserEmail();
            if (currentUser.equalsIgnoreCase(event.getEmail())) {
                SessionContext.logout();
            }
        }
    }
}
