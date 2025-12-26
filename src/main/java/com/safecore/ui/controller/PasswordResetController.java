package com.safecore.ui.controller;

import com.safecore.business.service.PasswordResetService;
import com.safecore.business.service.PasswordResetServiceImpl;
import com.safecore.persistence.dao.UserDaoJpa;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


/**
 * Controller JavaFX per il reset della password.
 *
 * Responsabilità:
 * - Gestione input/output UI
 * - Nessuna logica di business
 *
 * Il flusso reale (email) è simulato mostrando il token a schermo.
 */
public class PasswordResetController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField tokenField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Label messageLabel;

    private final PasswordResetService resetService =
            new PasswordResetServiceImpl(new UserDaoJpa());


    /**
     * Gestisce la richiesta del token di reset.
     */
    @FXML
    private void handleRequestToken() {

        String email = emailField.getText();

        if (email.isBlank()) {
            showError("Email is required");
            return;
        }

        try {
            String token = resetService.requestReset(email);

            // Simulazione invio email
            showInfo("Reset token (simulated): " + token);

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Gestisce il reset effettivo della password.
     */
    @FXML
    private void handleResetPassword() {

        String email = emailField.getText();
        String token = tokenField.getText();
        String newPassword = newPasswordField.getText();

        if (email.isBlank() || token.isBlank() || newPassword.isBlank()) {
            showError("All fields are required");
            return;
        }

        try {
            resetService.resetPassword(email, token, newPassword);
            showSuccess("Password successfully reset");

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    // --- Metodi di utilità UI ---

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText(message);
    }
}
