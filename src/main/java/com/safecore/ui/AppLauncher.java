package com.safecore.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point JavaFX dell'applicazione.
 *
 * Responsabilità:
 * - Avviare l'app
 * - Caricare la prima vista
 *
 * NON contiene logica di business.
 */
public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("SafeCore – Secure Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
