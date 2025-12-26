package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.UserServiceImpl;
import com.safecore.persistence.dao.UserDaoJpa;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * Controller JavaFX per il login.
 *
 * Scelte SE:
 * - Nessuna logica di persistenza
 * - Nessuna query
 * - Usa SOLO il Service Layer
 */
public class LoginController {

    // Binding con FXML
    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    // Service (dependency injection manuale)
    private final UserService userService =
            new UserServiceImpl(new UserDaoJpa());

    /**
     * Gestisce il click sul bottone Login.
     */
    @FXML
    private void handleLogin() {

        String email = emailField.getText();
        String password = passwordField.getText();

        // Validazione minimale UI-level
        if (email.isBlank() || password.isBlank()) {
            messageLabel.setText("Inserisci email e password");
            return;
        }

        boolean success = userService
                .login(email, password)
                .isPresent();

        if (success) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Login riuscito!");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Credenziali non valide");
        }
    }
    @FXML
    private void handleForgotPassword() {

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/password_reset.fxml")
            );

            Scene scene = new Scene(loader.load());

            // Recupera lo stage corrente partendo da un nodo qualsiasi
            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setTitle("SafeCore – Reset Password");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            messageLabel.setText("Unable to open reset page");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

}
