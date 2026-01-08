package com.safecore.ui.navigation;

import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.context.ApplicationContext;

/**
 * Utility centralizzata per la navigazione tra scene JavaFX integrata con Spring.
 */
public final class SceneNavigator {

    private static final Duration FADE_DURATION = Duration.millis(300);

    // Riferimento al contesto di Spring per l'iniezione dei bean nei controller
    private static ApplicationContext springContext;

    private SceneNavigator() {
    }

    /**
     * Inizializza il navigatore con il contesto Spring.
     * Va chiamato nel metodo start() della tua applicazione.
     */
    public static void setContext(ApplicationContext context) {
        springContext = context;
    }

    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            // === ACCESS CONTROL ===
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                fxmlPath = "/com/safecore/ui/view/login.fxml";
                title = "SafeCore – Login";
            }

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));

            // === SPRING INTEGRATION ===
            // Questa riga risolve il NoSuchMethodException:
            // Dice a FXMLLoader di recuperare i controller dal contesto Spring
            if (springContext != null) {
                loader.setControllerFactory(springContext::getBean);
            }

            Parent newRoot = loader.load();
            Scene scene = stage.getScene();

            if (scene == null) {
                Scene newScene = new Scene(newRoot);
                stage.setScene(newScene);
                stage.setTitle(title);
                stage.show();
                playFadeIn(newRoot);
                return;
            }

            Parent oldRoot = scene.getRoot();

            // Animazione Fade-out
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
            e.printStackTrace(); // Utile per vedere errori nel caricamento FXML
            throw new RuntimeException("Impossibile caricare la scena: " + fxmlPath, e);
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