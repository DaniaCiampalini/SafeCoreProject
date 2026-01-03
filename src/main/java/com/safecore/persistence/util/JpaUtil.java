package com.safecore.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Questa è la nostra centrale elettrica per il database.
 * Ho usato il pattern Singleton per l'EntityManagerFactory perché è un oggetto "pesante":
 * crearlo costa tanto tempo e memoria, quindi lo facciamo una volta sola all'avvio.
 */
public final class JpaUtil {

    // La factory viene creata una volta sola all'avvio (caricamento della classe)
    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("safecorePU");

    private JpaUtil() {
        // Costruttore privato per evitare che qualcuno provi a fare 'new JpaUtil()'
    }

    /**
     * Fornisce un EntityManager per una singola unità di lavoro (es. una transazione).
     * Ogni DAO deve ricordarsi di chiuderlo dopo l'uso per non sprecare memoria!
     */
    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /**
     * Questo metodo serve per spegnere tutto in modo pulito quando chiudiamo l'app.
     * È fondamentale per rilasciare i lock sui file del database (specialmente con H2 o database locali)
     * e non lasciare connessioni appese che potrebbero corrompere i dati.
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("Persistence Layer: EntityManagerFactory chiusa con successo.");
        }
    }
}