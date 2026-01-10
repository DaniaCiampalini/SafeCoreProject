package com.safecore.ui.navigation;

import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.context.ApplicationContext;
import java.net.URL;

/**
 * Questo è il "vigile urbano" dell'app. Gestisce il traffico tra le varie finestre (scene).
 * La cosa figa è che integra Spring con JavaFX: quando carichiamo una nuova schermata,
 * chiediamo a Spring di darci il Controller, così l'iniezione delle dipendenze (@Autowired)
 * continua a funzionare anche nella UI.
 */
public final class SceneNavigator {

    // Quanto deve durare l'effetto "dissolvenza" tra una pagina e l'altra
    private static final Duration FADE_DURATION = Duration.millis(300);
    private static ApplicationContext springContext;

    private SceneNavigator() {
        // Classe utility: non vogliamo che nessuno crei un oggetto SceneNavigator
    }

    /**
     * Ci serve per passare il contesto di Spring così possiamo pescare i Controller.
     */
    public static void setContext(ApplicationContext context) {
        springContext = context;
    }

    /**
     * Il metodo principale per cambiare schermata.
     * Gestisce anche la sicurezza (se non sei loggato, ti rimanda al login).
     */
    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            // 1. Controllo Accessi (Security Guard)
            // Se provi a entrare nella dashboard senza esserti loggato, ti rispediamo indietro!
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                System.out.println("Alt! Accesso negato: devi prima fare il login.");
                fxmlPath = "/com/safecore/ui/view/login.fxml";
                title = "SafeCore – Login";
            }

            // 2. Caricamento FXML con Spring
            URL fxmlResource = SceneNavigator.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                throw new RuntimeException("Cavolo, non trovo il file FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);

            // Questa è la parte magica: diciamo a JavaFX di usare Spring per creare i Controller.
            if (springContext != null) {
                loader.setControllerFactory(springContext::getBean);
            }

            Parent root = loader.load();
            Scene scene = stage.getScene();

            // 3. Gestione Scena e Transizioni
            if (scene == null) {
                // Se è la prima volta che apriamo una finestra, creiamo la scena
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                // Se c'era già qualcosa, facciamo un bel passaggio fluido
                playTransition(scene, root);
            }

            // 4. Carichiamo lo stile grafico (CSS)
            applyGlobalStyles(scene);

            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            System.err.println("BRUTTA NOTIZIA: Errore navigazione [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore critico durante il cambio scena.", e);
        }
    }

    /**
     * Fa sparire la vecchia schermata e apparire quella nuova in modo morbido.
     */
    private static void playTransition(Scene scene, Parent newRoot) {
        Parent oldRoot = scene.getRoot();

        // Dissolvenza in uscita (Fade Out)
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, oldRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            newRoot.setOpacity(0); // Inizia da trasparente
            scene.setRoot(newRoot);

            // Dissolvenza in entrata (Fade In)
            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Applica il file CSS a tutte le scene, così non dobbiamo farlo a mano ogni volta.
     */
    private static void applyGlobalStyles(Scene scene) {
        URL cssResource = SceneNavigator.class.getResource("/style.css");
        if (cssResource != null) {
            String cssPath = cssResource.toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        }
    }

    /**
     * Definisce quali pagine richiedono che l'utente sia autenticato.
     */
    private static boolean isProtectedScene(String fxmlPath) {
        return fxmlPath.contains("dashboard") || fxmlPath.contains("vault");
    }
}