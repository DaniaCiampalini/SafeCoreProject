package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.UserServiceImpl;
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

public class RegisterController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    private final UserService userService =
            new UserServiceImpl(new UserDaoJpa());

    @FXML
    private void handleGeneratePassword() {
        String generated = PasswordGenerator.generate(12);
        passwordField.setText(generated);
        confirmPasswordField.setText(generated);
        showInfo("Secure password generated");
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

        if (PasswordStrengthEvaluator.evaluate(password)
                == PasswordStrengthEvaluator.Strength.WEAK) {
            showError("Password too weak");
            return;
        }

        try {
            userService.register(email, password);
            showSuccess("Registration successful");

            userService.register(email, password);
            showSuccess("Registration successful");
            disableForm();


        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    /**
     * Navigazione verso login.
     */
    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/login.fxml", "SafeCore – Login");
    }


    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(msg);
    }

    private void showInfo(String msg) {
        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText(msg);
    }

    private void disableForm() {
        emailField.setDisable(true);
        passwordField.setDisable(true);
        confirmPasswordField.setDisable(true);
    }

}
