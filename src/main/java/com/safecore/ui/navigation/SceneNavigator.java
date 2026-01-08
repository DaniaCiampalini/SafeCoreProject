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

public final class SceneNavigator {

    private static final Duration FADE_DURATION = Duration.millis(300);
    private static ApplicationContext springContext;

    private SceneNavigator() {}

    public static void setContext(ApplicationContext context) {
        springContext = context;
    }

    public static void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            // Controllo Accessi
            if (isProtectedScene(fxmlPath) && !SessionContext.isLoggedIn()) {
                fxmlPath = "/com/safecore/ui/view/login.fxml";
                title = "SafeCore – Login";
            }

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));

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

            // APPLICAZIONE CSS GLOBALE
            URL cssResource = SceneNavigator.class.getResource("/style.css");
            if (cssResource != null) {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            stage.setTitle(title);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Errore navigazione: " + fxmlPath, e);
        }
    }

    private static void playTransition(Scene scene, Parent newRoot) {
        Parent oldRoot = scene.getRoot();
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, oldRoot);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            scene.setRoot(newRoot);
            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, newRoot);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private static boolean isProtectedScene(String fxmlPath) {
        return fxmlPath.contains("dashboard");
    }
}