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

/**
 * Controller per il recupero password.
 * Se un utente dimentica la password, può richiedere un "token" di reset.
 * In questa versione demo, il token viene mostrato direttamente a video invece
 * di essere inviato per email.
 */
@Component
public class PasswordResetController {

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label messageLabel;

    private final PasswordResetService resetService;
    private final PasswordGenerator passwordGenerator; 

    public PasswordResetController(PasswordResetService resetService, PasswordGenerator passwordGenerator) {
        this.resetService = resetService;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Chiede al sistema di generare un token di reset per l'email inserita.
     */
    @FXML
    private void handleRequestToken() {
        String email = emailField.getText();
        if (email == null || email.isBlank()) {
            showError("L'email è obbligatoria");
            return;
        }
        try {
            // Simuliamo l'invio: il service genera il token e noi lo mostriamo
            String token = resetService.requestReset(email);
            showInfo("Token di Reset (simulato): " + token);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Usa il token ricevuto per impostare una nuova password.
     */
    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();
        String token = tokenField.getText();
        String newPassword = newPasswordField.getText();

        if (isInputInvalid(email, token, newPassword)) {
            showError("Tutti i campi sono obbligatori");
            return;
        }

        try {
            // Se il token è valido, la password viene aggiornata
            resetService.resetPassword(email, token, newPassword);
            showSuccess("Password resettata con successo!");
            disableResetForm();
            redirectToLoginAfterDelay();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Anche qui, aiutiamo l'utente a scegliere una password forte.
     */
    @FXML
    private void handleGeneratePassword() {
        try {
            String generated = passwordGenerator.generateSafe(16);
            newPasswordField.setText(generated);
            showInfo("Password sicura generata!");
        } catch (Exception e) {
            showError("Errore durante la generazione");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
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
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void disableResetForm() {
        emailField.setDisable(true);
        tokenField.setDisable(true);
        newPasswordField.setDisable(true);
    }
}