package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.UserServiceImpl;
import com.safecore.persistence.dao.UserDaoJpa;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller JavaFX per il Login.
 *
 * Responsabilità:
 * - Input UI
 * - Invocazione service
 * - Inizializzazione sessione
 */
public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserServiceImpl(new UserDaoJpa());

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isBlank() || password.isBlank()) {
            showError("All fields are required");
            return;
        }

        try {
            userService.login(email, password);
            SessionContext.login(email);

            Stage stage = (Stage) messageLabel.getScene().getWindow();
            // Corretto percorso risorsa
            SceneNavigator.switchTo(stage, "/com/safecore/ui/dashboard.fxml", "SafeCore – Dashboard");

        } catch (Exception e) {
            showError("Invalid credentials or error: " + e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/register.fxml", "SafeCore – Register");
    }

    @FXML
    private void handleGoToReset() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/password_reset.fxml", "SafeCore – Password Reset");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }
}