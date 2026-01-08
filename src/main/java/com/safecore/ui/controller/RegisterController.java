package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.business.service.PasswordHintService; // Servizio esistente
import com.safecore.business.hints.PasswordHint;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class RegisterController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;
    @FXML private Label passwordStrengthLabel;

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField passwordTextField;
    @FXML private TextField confirmPasswordTextField;

    @FXML private Button togglePwdBtn;
    @FXML private Button toggleConfirmPwdBtn;

    private final UserService userService;
    private final PasswordGenerator passwordGenerator;
    private final PasswordHintService hintService; // Per il controllo reale della forza

    private boolean isPwdVisible = false;
    private boolean isConfirmVisible = false;

    public RegisterController(UserService userService,
                              PasswordGenerator passwordGenerator,
                              PasswordHintService hintService) {
        this.userService = userService;
        this.passwordGenerator = passwordGenerator;
        this.hintService = hintService;
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText();
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
            showSuccess("Account creato! Reindirizzamento...");
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
    private void handlePasswordTyping() {
        String pwd = isPwdVisible ? passwordTextField.getText() : passwordField.getText();

        if (pwd.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }

        // Corretta la logica
        List<PasswordHint> hints = hintService.getHints(pwd);

        if (hints.isEmpty()) {
            passwordStrengthLabel.setText("SICURA (Forte)");
            passwordStrengthLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 13px;");
        } else {
            // Mostriamo il primo suggerimento (es: "Mancano maiuscole")
            String helpMessage = hints.get(0).getMessage();
            passwordStrengthLabel.setText("DEBOLE: " + helpMessage);
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        }
    }

    @FXML
    private void handleGeneratePassword() {
        String generated = passwordGenerator.generateSafe(14);
        passwordField.setText(generated);
        passwordTextField.setText(generated);
        confirmPasswordField.setText(generated);
        confirmPasswordTextField.setText(generated);

        showInfo("Password generata con successo");
        handlePasswordTyping();
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
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
    private void showInfo(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    private void disableForm() {
        emailField.setDisable(true);
        passwordField.setDisable(true);
        passwordTextField.setDisable(true);
        confirmPasswordField.setDisable(true);
        confirmPasswordTextField.setDisable(true);
    }
}