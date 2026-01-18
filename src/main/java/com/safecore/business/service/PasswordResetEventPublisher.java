package com.safecore.business.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject semplice per notificare gli observer dei reset password completati.
 * Utilizza una CopyOnWriteArrayList per la gestione thread-safe degli observer.
 */

@Component
public class PasswordResetEventPublisher {

    private final List<PasswordResetObserver> observers = new CopyOnWriteArrayList<>();

    public void register(PasswordResetObserver observer) {
        Objects.requireNonNull(observer, "Observer nullo non consentito");
        observers.add(observer);
    }

    public void unregister(PasswordResetObserver observer) {
        if (observer != null) {
            observers.remove(observer);
        }
    }

    public void publish(PasswordResetCompletedEvent event) {
        List<PasswordResetObserver> snapshot = new ArrayList<>(observers);
        snapshot.sort(Comparator.comparing(o -> o.getClass().getName()));

        for (PasswordResetObserver observer : snapshot) {
            try {
                observer.onPasswordResetCompleted(event);
            } catch (RuntimeException ex) {
                System.err.println("Observer reset password fallito: " + ex.getMessage());
            }
        }
    }
}
