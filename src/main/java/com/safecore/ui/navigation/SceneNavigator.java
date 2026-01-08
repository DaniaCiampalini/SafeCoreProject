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
 * Gestisce la navigazione tra le scene integrando il contesto Spring
 * per l'iniezione delle dipendenze nei controller JavaFX.
 */
public final class SceneNavigator {

    private static final Duration FADE_DURATION = Duration.millis(300);
    private static ApplicationContext springContext;

    private SceneNavigator() {
        // Costruttore privato per classe utility
    }

    /**
     * Configura il contesto Spring. Deve essere chiamato nell'init() dell'AppLauncher.
     */
    public static void setContext(ApplicationContext context) {
        springContext = context;
    }

    /**
     * Cambia la scena corrente con una transizione di dissolvenza.
     */
    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            // 1. Controllo Accessi (Security Guard)
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                System.out.println("Accesso negato: reindirizzamento al login.");
                fxmlPath = "/com/safecore/ui/view/login.fxml";
                title = "SafeCore – Login";
            }

            // 2. Caricamento FXML con Spring
            URL fxmlResource = SceneNavigator.class.getResource(fxmlPath);
            if (fxmlResource == null) {
                throw new RuntimeException("Impossibile trovare il file FXML: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlResource);

            // Inietta i bean di Spring nei controller JavaFX
            if (springContext != null) {
                loader.setControllerFactory(springContext::getBean);
            }

            Parent root = loader.load();
            Scene scene = stage.getScene();

            // 3. Gestione Scena e Transizioni
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
            } else {
                playTransition(scene, root);
            }

            // 4. Applicazione CSS Globale
            applyGlobalStyles(scene);

            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            System.err.println("ERRORE NAVIGAZIONE [" + fxmlPath + "]: " + e.getMessage());
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
            newRoot.setOpacity(0); // Parte invisibile per il fade-in
            scene.setRoot(newRoot);

            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private static void applyGlobalStyles(Scene scene) {
        URL cssResource = SceneNavigator.class.getResource("/style.css");
        if (cssResource != null) {
            String cssPath = cssResource.toExternalForm();
            if (!scene.getStylesheets().contains(cssPath)) {
                scene.getStylesheets().add(cssPath);
            }
        }
    }

    private static boolean isProtectedScene(String fxmlPath) {
        // Protegge tutte le view tranne login e registrazione
        return fxmlPath.contains("dashboard") || fxmlPath.contains("vault");
    }
}