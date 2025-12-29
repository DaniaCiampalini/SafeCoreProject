package com.safecore.ui.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility per la navigazione tra scene JavaFX.
 *
 * SE:
 * - Centralizza il caricamento delle viste
 * - Evita duplicazione di codice nei controller
 */
public final class SceneNavigator {

    private SceneNavigator() {
    }

    public static void switchTo(Stage stage, String fxml, String title) {

        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneNavigator.class.getResource(fxml)
            );
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);

        } catch (Exception e) {
            throw new RuntimeException("Unable to load view: " + fxml, e);
        }
    }
}
