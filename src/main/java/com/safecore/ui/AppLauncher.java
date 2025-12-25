package com.safecore.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point JavaFX dell'applicazione.
 *
 * Responsabilità:
 * - Avviare l'applicazione JavaFX
 * - Caricare la prima vista (Login / Reset)
 *
 * NON contiene:
 * - logica di business
 * - accesso a database
 * - logica di sicurezza
 */
public class AppLauncher extends Application {

    @Override
    public void start(Stage stage) {

        try {
            // Caricamento della vista iniziale
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/safecore/ui/login.fxml")
                    // oppure: reset-password.fxml se vuoi partire da lì
            );

            Scene scene = new Scene(loader.load(), 400, 450);

            stage.setTitle("SafeCore – Secure Login");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            // Errore critico: l'app non può partire
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
