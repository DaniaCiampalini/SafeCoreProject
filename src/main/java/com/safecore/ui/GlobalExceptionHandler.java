package com.safecore.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Component;

/**
 * Questa classe è come il pronto soccorso dell'app.
 * Se qualcosa va storto e viene lanciata un'eccezione che nessuno ha catturato,
 * finisce qui. Invece di far sparire l'app nel nulla, mostriamo un bel messaggio di errore all'utente.
 */
@Component
public class GlobalExceptionHandler {

    /**
     * Gestisce le eccezioni generiche, mostrandole in un popup di errore.
     */
    public void handle(Throwable e) {
        // Stampiamo comunque l'errore in console, che per il debug serve sempre.
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
}
