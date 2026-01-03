package com.safecore.ui.controller;

import com.safecore.business.service.PasswordResetService;
import com.safecore.business.service.PasswordResetServiceImpl;
import com.safecore.persistence.dao.PasswordResetTokenDaoJpa;
import com.safecore.persistence.dao.UserDaoJpa;
import com.safecore.security.PasswordGenerator;
import com.safecore.security.PasswordStrengthEvaluator;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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
            new PasswordResetServiceImpl(new UserDaoJpa(), new PasswordResetTokenDaoJpa());


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

        // Controllo robustezza password
        if (PasswordStrengthEvaluator.evaluate(newPassword)
                == PasswordStrengthEvaluator.Strength.WEAK) {
            showError("Password too weak");
            return;
        }

        try {
            resetService.resetPassword(email, token, newPassword);
            showSuccess("Password successfully reset");
            // disabilita il form (già fatto)
            disableResetForm();
            // redirect automatico dopo 2 secondi
            redirectToLoginAfterDelay();



        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    @FXML
    private void handleGeneratePassword() {

        try {
            String generated = PasswordGenerator.generate(16);
            newPasswordField.setText(generated);

            messageLabel.setStyle("-fx-text-fill: blue;");
            messageLabel.setText("Secure password generated");

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }

    /**
     * Navigazione verso la schermata di login.
     */
    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/login.fxml", "SafeCore – Login");
    }

    private void redirectToLoginAfterDelay() {

        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2 secondi
            } catch (InterruptedException ignored) {}

            javafx.application.Platform.runLater(() -> {
                Stage stage = (Stage) messageLabel.getScene().getWindow();
                SceneNavigator.switchTo(stage, "/login.fxml",
                        "SafeCore – Login");
            });
        }).start();
    }


    // Metodi di utilità UI

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

    private void disableResetForm() {
        emailField.setDisable(true);
        tokenField.setDisable(true);
        newPasswordField.setDisable(true);
    }

}
