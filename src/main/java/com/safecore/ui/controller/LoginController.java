package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        if (email.isBlank() || password.isBlank()) {
            showError("Campi obbligatori");
            return;
        }
        userService.login(email, password).ifPresentOrElse(
                user -> {
                    SessionContext.login(email);
                    navigateTo("/com/safecore/ui/view/dashboard.fxml", "SafeCore – Dashboard");
                },
                () -> showError("Credenziali errate")
        );
    }

    @FXML
    private void handleGoToRegister() {
        navigateTo("/com/safecore/ui/view/register.fxml", "SafeCore – Registrazione");
    }

    @FXML
    private void handleForgotPassword() {
        navigateTo("/com/safecore/ui/view/password_reset.fxml", "SafeCore – Reset Password");
    }

    private void navigateTo(String path, String title) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, path, title);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }
}