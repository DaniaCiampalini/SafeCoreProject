package com.safecore.ui.navigation;

import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Utility centralizzata per la navigazione tra scene JavaFX.
 *
 * Responsabilità:
 * - Cambio scena
 * - Protezione scene
 * - Animazioni di transizione
 *
 * Scelte SE:
 * - Punto unico di navigazione
 * - Nessuna logica nei controller
 * - UX consistente
 */
public final class SceneNavigator {

    private static final Duration FADE_DURATION = Duration.millis(300);

    private SceneNavigator() {
    }

    public static void switchTo(Stage stage, String fxmlPath, String title) {

        try {
            // === ACCESS CONTROL ===
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                fxmlPath = "/login.fxml";
                title = "SafeCore – Login";
            }

            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource(fxmlPath)
            );

            Parent newRoot = loader.load();

            Scene scene = stage.getScene();

            if (scene == null) {
                // primo caricamento (app start)
                Scene newScene = new Scene(newRoot);
                stage.setScene(newScene);
                stage.setTitle(title);
                stage.show();
                playFadeIn(newRoot);
                return;
            }

            Parent oldRoot = scene.getRoot();

            // Fade-out della scena corrente
            FadeTransition fadeOut = new FadeTransition(FADE_DURATION, oldRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            String finalTitle = title;
            fadeOut.setOnFinished(event -> {
                scene.setRoot(newRoot);
                stage.setTitle(finalTitle);
                playFadeIn(newRoot);
            });

            fadeOut.play();

        } catch (Exception e) {
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }

    private static void playFadeIn(Parent root) {
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private static boolean isProtectedScene(String fxmlPath) {
        return fxmlPath.contains("dashboard");
    }
}

