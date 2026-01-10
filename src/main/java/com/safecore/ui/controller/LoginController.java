package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/**
 * Controller per la schermata di Login.
 * Gestisce l'inserimento dell'email e della password, con la possibilità di vedere
 * la password in chiaro (comodo se hai le dita grosse e sbagli sempre).
 */
@Component
public class LoginController {

    // Campi collegati al file login.fxml
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField; // Campo "di riserva" per mostrare la password
    @FXML private Button togglePasswordBtn;     // Il tasto con l'occhio
    @FXML private Label messageLabel;           // Dove scriviamo "Credenziali errate"

    private final UserService userService;
    private boolean isPasswordVisible = false;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Cosa succede quando clicchi "Login"
     */
    @FXML
    private void handleLogin() {
        // 1. Prendiamo l'email e puliamola (niente spazi inutili)
        String email = emailField.getText() != null ? emailField.getText().trim().toLowerCase() : "";

        // 2. Capiamo quale campo password stiamo usando (quello coi pallini o quello in chiaro)
        String password;
        if (passwordTextField.isVisible()) {
            password = passwordTextField.getText();
        } else {
            password = passwordField.getText();
        }

        // 3. Validazione base: se i campi sono vuoti, non ci proviamo neanche
        if (email.isBlank() || password == null || password.isBlank()) {
            showError("Email e password sono obbligatorie");
            return;
        }

        // Chiediamo al servizio se le credenziali sono giuste.
        // Se tutto va bene, salviamo l'utente in sessione e andiamo in dashboard.
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
     * Questa è una finezza: scambia il campo password con un campo testo normale
     * così puoi vedere cosa stai scrivendo.
     */
    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Torna ai pallini
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            togglePasswordBtn.setText("👁");
        } else {
            // Mostra testo in chiaro
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

    /**
     * Helper per cambiare schermata
     */
    private void navigateTo(String path, String title) {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, path, title);
    }

    /**
     * Mostra un messaggio di errore rosso in basso
     */
    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
}