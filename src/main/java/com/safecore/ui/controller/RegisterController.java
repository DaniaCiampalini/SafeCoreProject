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

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            showError("All fields are required");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match");
            return;
        }

        try {
            // Il controllo robustezza è ora delegato al Service
            userService.register(email, password);
            showSuccess("Registration successful! Redirecting...");
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
        String generated = PasswordGenerator.generate(12);
        passwordField.setText(generated);
        confirmPasswordField.setText(generated);
        showInfo("Secure password generated");
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/login.fxml", "SafeCore – Login");
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