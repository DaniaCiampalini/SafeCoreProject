package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService;

    // Spring inietta automaticamente UserServiceImpl qui
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError("All fields are required");
            return;
        }

        try {
            // Se le credenziali sono errate, il service lancerà un'eccezione o restituirà Optional vuoto
            if (userService.login(email, password).isPresent()) {
                SessionContext.login(email);
                Stage stage = (Stage) messageLabel.getScene().getWindow();
                SceneNavigator.switchTo(stage, "/com/safecore/ui/dashboard.fxml", "SafeCore – Dashboard");
            } else {
                showError("Invalid email or password");
            }

        } catch (Exception e) {
            showError("System error: " + e.getMessage());
        }
    }

    @FXML private void handleGoToRegister() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/register.fxml", "SafeCore – Register");
    }

    @FXML private void handleGoToReset() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/password_reset.fxml", "SafeCore – Password Reset");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }
}