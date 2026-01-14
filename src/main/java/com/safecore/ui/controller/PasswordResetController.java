package com.safecore.ui.controller;

import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.business.service.PasswordResetCompletedEvent;
import com.safecore.business.service.PasswordResetEventPublisher;
import com.safecore.business.service.PasswordResetObserver;
import com.safecore.business.service.PasswordResetRequestResult;
import com.safecore.business.service.PasswordResetService;
import com.safecore.security.PasswordGenerator; // Import corretto
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.time.format.DateTimeFormatter;

/**
 * Controller per il recupero password.
 * Se un utente dimentica la password, può richiedere un "token" di reset.
 * In questa versione demo, il token viene mostrato direttamente a video invece
 * di essere inviato per email.
 */
@Component
public class PasswordResetController implements PasswordResetObserver {

    @FXML private TextField emailField;
    @FXML private TextField tokenField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label messageLabel;

    private final PasswordResetService resetService;
    private final PasswordGenerator passwordGenerator;
    private final PasswordResetEventPublisher eventPublisher;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public PasswordResetController(PasswordResetService resetService,
                                   PasswordGenerator passwordGenerator,
                                   PasswordResetEventPublisher eventPublisher) {
        this.resetService = resetService;
        this.passwordGenerator = passwordGenerator;
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    void registerObserver() {
        eventPublisher.register(this);
    }

    @PreDestroy
    void unregisterObserver() {
        eventPublisher.unregister(this);
    }

    /**
     * Chiede al sistema di generare un token di reset per l'email inserita.
     */
    @FXML
    private void handleRequestToken() {
        String email = emailField.getText();
        if (email == null || email.isBlank()) {
            showError("L'email è obbligatoria");
            return;
        }
        try {
            // Simuliamo l'invio: il service genera il token e noi lo mostriamo
            PasswordResetRequestResult result = resetService.requestReset(email);
            showInfo("Token generato (valido fino alle " + result.getExpiresAt().format(timeFormatter) + "): " + result.getToken());
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    /**
     * Usa il token ricevuto per impostare una nuova password.
     */
    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();
        String token = tokenField.getText();
        String newPassword = newPasswordField.getText();

        if (isInputInvalid(email, token, newPassword)) {
            showError("Tutti i campi sono obbligatori");
            return;
        }

        try {
            // Se il token è valido, la password viene aggiornata
            resetService.resetPassword(email, token, newPassword);
        } catch (InvalidTokenException ex) {
            showError("Token non valido o scaduto. Richiedi un nuovo token e riprova.");
        } catch (UserNotFoundException ex) {
            showError("Nessun account trovato per questa email.");
        } catch (Exception e) {
            showError("Errore durante il reset: " + e.getMessage());
        }
    }

    /**
     * Anche qui, aiutiamo l'utente a scegliere una password forte.
     */
    @FXML
    private void handleGeneratePassword() {
        try {
            String generated = passwordGenerator.generateSafe(16);
            newPasswordField.setText(generated);
            showInfo("Password sicura generata!");
        } catch (Exception e) {
            showError("Errore durante la generazione");
        }
    }

    @FXML
    private void handleBackToLogin() {
        Stage stage = (Stage) emailField.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    private boolean isInputInvalid(String email, String token, String pwd) {
        return email == null || email.isBlank() || token == null || token.isBlank() || pwd == null || pwd.isBlank();
    }

    private void redirectToLoginAfterDelay() {
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            Platform.runLater(this::handleBackToLogin);
        }).start();
    }

    @Override
    public void onPasswordResetCompleted(PasswordResetCompletedEvent event) {
        Platform.runLater(() -> {
            showSuccess("Password aggiornata per " + event.getEmail() + ". Effettua di nuovo il login.");
            disableResetForm();
            redirectToLoginAfterDelay();
        });
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showInfo(String message) {
        messageLabel.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void disableResetForm() {
        emailField.setDisable(true);
        tokenField.setDisable(true);
        newPasswordField.setDisable(true);
    }
}