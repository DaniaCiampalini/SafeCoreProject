package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    // 1. DICHIARA QUI IL COMPONENTE FXML (così JavaFX lo inizializza all'avvio)
    @FXML private Label passwordStrengthLabel;

    private final UserService userService;
    private final PasswordGenerator passwordGenerator;

    public RegisterController(UserService userService, PasswordGenerator passwordGenerator) {
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            showError("Tutti i campi sono obbligatori");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Le password non coincidono");
            return;
        }

        try {
            userService.register(email, password);
            showSuccess("Registrazione completata! Reindirizzamento...");
            disableForm();

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(this::handleBackToLogin);
            }).start();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePassword() {
        String generated = passwordGenerator.generateSafe(12);
        passwordField.setText(generated);
        confirmPasswordField.setText(generated);
        showInfo("Password sicura generata");
        // Aggiorna anche il label della forza quando generi
        handlePasswordTyping();
    }

    @FXML
    private void handlePasswordTyping() {
        String pwd = passwordField.getText();

        // 2. RIMOSSA LA DICHIARAZIONE LOCALE: ora usa il campo @FXML dichiarato sopra
        if (pwd.isEmpty()) {
            passwordStrengthLabel.setText("");
        } else if (pwd.length() < 8) {
            passwordStrengthLabel.setText("Debole");
            passwordStrengthLabel.setStyle("-fx-text-fill: red;");
        } else {
            passwordStrengthLabel.setText("Forte");
            passwordStrengthLabel.setStyle("-fx-text-fill: green;");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    private void showError(String msg) { messageLabel.setStyle("-fx-text-fill: red;"); messageLabel.setText(msg); }
    private void showSuccess(String msg) { messageLabel.setStyle("-fx-text-fill: green;"); messageLabel.setText(msg); }
    private void showInfo(String msg) { messageLabel.setStyle("-fx-text-fill: blue;"); messageLabel.setText(msg); }

    private void disableForm() {
        emailField.setDisable(true);
        passwordField.setDisable(true);
        confirmPasswordField.setDisable(true);
    }
}