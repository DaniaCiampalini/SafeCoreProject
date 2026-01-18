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
 * Utility per la navigazione tra le schermate dell'app JavaFX.
 * Supporta transizioni fluide e l'integrazione con Spring per i controller.
 * Gestisce anche la sicurezza delle pagine protette.
 */

public final class SceneNavigator {

    private static final Duration FADE_DURATION = Duration.millis(300);
    private static ApplicationContext springContext;

    private SceneNavigator() {
        // Classe utility: vogliamo che nessuno crei un oggetto SceneNavigator
    }

    // Iniettiamo il contesto Spring all'avvio dell'app
    public static void setContext(ApplicationContext context) {
        springContext = context;
    }

    /**
     * Il metodo principale per cambiare schermata.
     * Gestisce anche la sicurezza: se la pagina è protetta e l'utente non è loggato,
     * lo rimanda alla pagina di login.
     */
    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                System.out.println("Alt! Accesso negato: devi prima fare il login.");
                fxmlPath = "/com/safecore/ui/view/login.fxml";
                title = "SafeCore – Login";
            }

            URL fxmlResource = SceneNavigator.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                throw new RuntimeException("Cavolo, non trovo il file FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);

            // Integrazione Spring-JavaFX, i controller li prende da Spring
            if (springContext != null) {
                loader.setControllerFactory(springContext::getBean);
            }

            Parent root = loader.load();
            Scene scene = stage.getScene();

            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                playTransition(scene, root);
            }

            applyGlobalStyles(scene);

            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            System.err.println("Errore navigazione [" + fxmlPath + "]: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Errore critico durante il cambio scena.", e);
        }
    }


    private static void playTransition(Scene scene, Parent newRoot) {
        Parent oldRoot = scene.getRoot();

        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, oldRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            newRoot.setOpacity(0);
            scene.setRoot(newRoot);

            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    /**
     * Applica il file CSS a tutte le scene per unformare lo stile grafico.
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