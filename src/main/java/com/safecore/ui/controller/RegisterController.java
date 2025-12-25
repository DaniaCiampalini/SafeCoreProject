package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.UserServiceImpl;
import com.safecore.persistence.dao.UserDaoJpa;
import com.safecore.business.hints.PasswordHint;
import com.safecore.business.hints.PasswordHintService;
import com.safecore.business.hints.HintLevel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller JavaFX per la registrazione.
 *
 * Smart Hints:
 * - Suggerimenti NON vincolanti sulla sicurezza
 * - Supporto decisionale all'utente
 */
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

    private final PasswordHintService hintService =
            new PasswordHintService();

    @FXML
    private void handlePasswordTyping() {
        showHints(passwordField.getText());
    }
//così mentre l'utente digita, il sistema consiglia ma non blocca

    private void showHints(String password) {

        var hints = hintService.evaluate(password);

        if (hints.isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Password sicura");
        } else {
            PasswordHint hint = hints.get(0);
            messageLabel.setStyle(
                    hint.getLevel() == HintLevel.WARNING
                            ? "-fx-text-fill: orange;"
                            : "-fx-text-fill: blue;"
            );
            messageLabel.setText(hint.getMessage());
        }
    }

    @FXML
    private void handleRegister() {

        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Tutti i campi sono obbligatori");
            return;
        }

        if (!password.equals(confirmPassword)) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Le password non coincidono");
            return;
        }

        try {
            userService.register(email, password);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Registrazione completata!");
        } catch (IllegalArgumentException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }
}
