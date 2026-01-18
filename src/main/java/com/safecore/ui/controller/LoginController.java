package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/**
 * Controller per la schermata di Login.
 * Gestisce l'autenticazione e il setup iniziale della sessione utente.
 */
@Component
public class LoginController {

    private final UserService userService;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private Button togglePasswordBtn;
    @FXML
    private Label messageLabel;
    private boolean isPasswordVisible = false;

    public LoginController(UserService userService) {
        this.userService = userService;
    }


    @FXML
    private void handleLogin() {
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";

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
     * Sincronizza i due campi (Password/Text) per simulare l'effetto "mostra password".
     * Questo approccio espone brevemente la password in memoria come String
     * in un campo TextField non protetto.
     */
    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            togglePasswordBtn.setText("Show");
        } else {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePasswordBtn.setText("Hide");
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

    /**
     * Helper per cambiare schermata
     */
    private void navigateTo(String path, String title) {
        // Recuperiamo lo stage dai componenti esistenti per evitare dipendenze esterne
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, path, title);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}