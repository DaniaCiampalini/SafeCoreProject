package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.UserServiceImpl;
import com.safecore.persistence.dao.UserDaoJpa;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UserService userService =
            new UserServiceImpl(new UserDaoJpa());

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

            // salva sessione
            UserSession.login(email);

            // naviga a dashboard
            Stage stage = (Stage) messageLabel.getScene().getWindow();
            SceneNavigator.switchTo(
                    stage,
                    "/dashboard.fxml",
                    "SafeCore – Dashboard"
            );

        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/register.fxml", "SafeCore – Register");
    }

    @FXML
    private void handleGoToReset() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/password_reset.fxml",
                "SafeCore – Password Reset");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }
}
