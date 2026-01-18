package com.safecore.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Component;

/**
 * Gestore globale delle eccezioni.
 * Mostra un popup di errore ogni volta che viene chiamato il metodo handle.
 */
@Component
public class GlobalExceptionHandler {

    /**
     * Metodo statico comodo per mostrare errori veloci ovunque nel codice.
     */
    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }


    public void handle(Throwable e) {
        e.printStackTrace();

        // Usiamo Platform.runLater perché gli Alert di JavaFX devono girare sul thread della UI.
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ops! Qualcosa è andato storto");
            alert.setHeaderText("Errore di Sistema");
            alert.setContentText("Messaggio: " + e.getMessage());
            alert.getButtonTypes().setAll(ButtonType.OK);
            alert.showAndWait();
        });
    }
}
