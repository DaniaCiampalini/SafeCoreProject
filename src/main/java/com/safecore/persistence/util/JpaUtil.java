package com.safecore.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Classe di utilità per la gestione centralizzata di JPA.
 *
 * Scelta progettuale:
 * - Singleton implicito tramite campo statico
 * - Una sola EntityManagerFactory per tutta l'applicazione
 *
 * Motivazione SE:
 * - Evita sprechi di risorse
 * - Centralizza la configurazione
 * - Facilita test e manutenzione
 */
public final class JpaUtil {

    // Factory unica, costosa da creare → inizializzata una sola volta
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("safecorePU");

    // Costruttore privato → classe non istanziabile
    private JpaUtil() {}

    /**
     * Fornisce un EntityManager per una singola unità di lavoro.
     * Ogni DAO deve chiuderlo dopo l'uso.
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Chiusura ordinata della factory (es. allo shutdown app).
     */
    public static void close() {
        emf.close();
    }
}
