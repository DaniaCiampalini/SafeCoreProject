package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;
    @FXML private Label passwordStrengthLabel;

    // Campi Password (Nascosti)
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Campi Testo (In chiaro)
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;

    // Bottoni Occhio
    @FXML private Button togglePwdBtn;
    @FXML private Button toggleConfirmPwdBtn;

    private final UserService userService;
    private final PasswordGenerator passwordGenerator;

    private boolean isPwdVisible = false;
    private boolean isConfirmVisible = false;

    public RegisterController(UserService userService, PasswordGenerator passwordGenerator) {
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
        // Recupera i valori dai campi attivi
        String password = isPwdVisible ? passwordTextField.getText() : passwordField.getText();
        String confirm = isConfirmVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();

        if (email.isBlank() || password.isBlank() || confirm.isBlank()) {
            showError("Tutti i campi sono obbligatori");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Le password non coincidono");
            return;
        }

        try {
            userService.register(email, password);
            showSuccess("Registrazione completata! Reindirizzamento...");
            disableForm();

            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
                Platform.runLater(this::handleBackToLogin);
            }).start();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        if (isPwdVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            togglePwdBtn.setText("👁");
        } else {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            togglePwdBtn.setText("🙈");
        }
        isPwdVisible = !isPwdVisible;
    }

    @FXML
    private void toggleConfirmVisibility() {
        if (isConfirmVisible) {
            confirmPasswordField.setText(confirmPasswordTextField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordTextField.setVisible(false);
            toggleConfirmPwdBtn.setText("👁");
        } else {
            confirmPasswordTextField.setText(confirmPasswordField.getText());
            confirmPasswordTextField.setVisible(true);
            confirmPasswordField.setVisible(false);
            toggleConfirmPwdBtn.setText("🙈");
        }
        isConfirmVisible = !isConfirmVisible;
    }

    @FXML
    private void handleGeneratePassword() {
        String generated = passwordGenerator.generateSafe(12);

        // Aggiorna entrambi i set di campi per sicurezza
        passwordField.setText(generated);
        passwordTextField.setText(generated);
        confirmPasswordField.setText(generated);
        confirmPasswordTextField.setText(generated);

        showInfo("Password sicura generata");
        handlePasswordTyping();
    }

    @FXML
    private void handlePasswordTyping() {
        // Legge dal campo corretto in base alla visibilità
        String pwd = isPwdVisible ? passwordTextField.getText() : passwordField.getText();

        if (pwd.isEmpty()) {
            passwordStrengthLabel.setText("");
        } else if (pwd.length() < 8) {
            passwordStrengthLabel.setText("Debole");
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc2626;");
        } else {
            passwordStrengthLabel.setText("Forte");
            passwordStrengthLabel.setStyle("-fx-text-fill: #16a34a;");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    // Helper per i messaggi
    private void showError(String msg) { messageLabel.setStyle("-fx-text-fill: #dc2626;"); messageLabel.setText(msg); }
    private void showSuccess(String msg) { messageLabel.setStyle("-fx-text-fill: #16a34a;"); messageLabel.setText(msg); }
    private void showInfo(String msg) { messageLabel.setStyle("-fx-text-fill: #2563eb;"); messageLabel.setText(msg); }

    private void disableForm() {
        emailField.setDisable(true);
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        confirmPasswordField.setDisable(true);
        confirmPasswordTextField.setDisable(true);
    }
}