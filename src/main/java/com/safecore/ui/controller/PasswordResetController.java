package com.safecore.ui.controller;

import com.safecore.business.service.PasswordResetService;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/**
 * Controller JavaFX gestito da Spring Boot.
 * Dimostra il disaccoppiamento totale dalla persistenza.
 */
@Component
public class PasswordResetController {

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label messageLabel;

    // Iniezione tramite interfaccia: non sappiamo (e non ci interessa)
    // come sia implementato il service o quale DB usi.
    private final PasswordResetService resetService;

    // Costruttore per la Dependency Injection di Spring
    public PasswordResetController(PasswordResetService resetService) {
        this.resetService = resetService;
    }

    /**
     * Richiede un token di reset delegando al Service.
     */
    @FXML
    private void handleRequestToken() {
        String email = emailField.getText();

        if (email == null || email.isBlank()) {
            showError("Email is required");
            return;
        }

        try {
            // Logica di business delegata al service
            String token = resetService.requestReset(email);
            showInfo("Reset token (simulated): " + token);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Esegue il reset della password.
     */
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
            // Il controllo robustezza ora è DENTRO resetPassword nel service
            resetService.resetPassword(email, token, newPassword);

            showSuccess("Password successfully reset");
            disableResetForm();
            redirectToLoginAfterDelay();

        } catch (IllegalArgumentException e) {
            // Cattura errori di business (password debole, token scaduto)
            showError(e.getMessage());
        } catch (Exception e) {
            // Cattura errori infrastrutturali
            showError("A system error occurred. Please try again.");
        }
    }

    @FXML
    private void handleGeneratePassword() {
        try {
            String generated = PasswordGenerator.generate(16);
            newPasswordField.setText(generated);
            showInfo("Secure password generated");
        } catch (Exception e) {
            showError("Error generating password");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/login.fxml", "SafeCore – Login");
    }

    // --- Metodi privati di supporto ---

    private boolean isInputInvalid(String email, String token, String pwd) {
        return email == null || email.isBlank() ||
                token == null || token.isBlank() ||
                pwd == null || pwd.isBlank();
    }

    private void redirectToLoginAfterDelay() {
        // Uso di un thread separato per non bloccare la UI (JavaFX Application Thread)
        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}

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
