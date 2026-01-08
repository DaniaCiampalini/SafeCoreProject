package com.safecore.ui.controller;

import com.safecore.business.service.PasswordResetService;
import com.safecore.security.PasswordGenerator; // Import corretto
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetController {

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label messageLabel;

    private final PasswordResetService resetService;
    private final PasswordGenerator passwordGenerator; // 1. AGGIUNTO CAMPO PER INIEZIONE

    // 2. AGGIUNTO AL COSTRUTTORE PER SPRING
    public PasswordResetController(PasswordResetService resetService, PasswordGenerator passwordGenerator) {
        this.resetService = resetService;
        this.passwordGenerator = passwordGenerator;
    }

    @FXML
    private void handleRequestToken() {
        String email = emailField.getText();
        if (email == null || email.isBlank()) {
            showError("Email is required");
            return;
        }
        try {
            String token = resetService.requestReset(email);
            showInfo("Reset token (simulated): " + token);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();
        String token = tokenField.getText();
        String newPassword = newPasswordField.getText();

        if (isInputInvalid(email, token, newPassword)) {
            showError("All fields are required");
            return;
        }

        try {
            resetService.resetPassword(email, token, newPassword);
            showSuccess("Password successfully reset");
            disableResetForm();
            redirectToLoginAfterDelay();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePassword() {
        try {
            // 3. CORRETTO: Uso dell'istanza iniettata e del metodo generateSafe
            String generated = passwordGenerator.generateSafe(16);
            newPasswordField.setText(generated);
            showInfo("Secure password generated");
        } catch (Exception e) {
            showError("Error generating password");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        // Assicurati che il path sia corretto rispetto alla tua struttura resources
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    private boolean isInputInvalid(String email, String token, String pwd) {
        return email == null || email.isBlank() || token == null || token.isBlank() || pwd == null || pwd.isBlank();
    }

    private void redirectToLoginAfterDelay() {
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            Platform.runLater(this::handleBackToLogin);
        }).start();
    }

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