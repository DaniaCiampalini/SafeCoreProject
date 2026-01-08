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
    @FXML private TextField passwordTextField; // Campo per password in chiaro
    @FXML private Button togglePasswordBtn;     // Bottone con l'icona dell'occhio
    @FXML private Label messageLabel;

    private final UserService userService;
    private boolean isPasswordVisible = false;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @FXML
    private void handleLogin() {
        // 1. Pulizia input: toglie spazi e mette tutto in minuscolo
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";

        // 2. Recupero password dal campo visibile
        String password;
        if (passwordTextField.isVisible()) {
            password = passwordTextField.getText();
        } else {
            password = passwordField.getText();
        }

        if (email.isBlank() || password == null || password.isBlank()) {
            showError("Email e password sono obbligatorie");
            return;
        }

        // DEBUG: Controlla la console di IntelliJ quando clicchi!
        System.out.println("LOG DEBUG - Tentativo Login:");
        System.out.println("Email: [" + email + "]");
        System.out.println("Password (lunghezza): " + password.length());

        userService.login(email, password).ifPresentOrElse(
                user -> {
                    System.out.println("LOGIN OK per: " + email);
                    SessionContext.login(email);
                    navigateTo("/com/safecore/ui/view/dashboard.fxml", "SafeCore – Dashboard");
                },
                () -> showError("Credenziali non valide")
        );
    }

    /**
     * Gestisce lo scambio tra PasswordField (pallini) e TextField (testo in chiaro)
     */
    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Torna ai pallini: copia il testo dal chiaro al nascosto
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            togglePasswordBtn.setText("👁");
        } else {
            // Mostra testo: copia il testo dal nascosto al chiaro
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordBtn.setText("🙈");
        }
        isPasswordVisible = !isPasswordVisible;
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
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}