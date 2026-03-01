package com.safecore.ui.controller;

import com.safecore.business.exception.InvalidTokenException;
import com.safecore.business.exception.UserNotFoundException;
import com.safecore.business.service.UserService;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.safecore.ui.GlobalExceptionHandler.showError;

@Component
public class SettingsController {

    private final UserService userService;

    @FXML private Label emailLabel;
    @FXML private TextField confirmationField;
    @FXML private Button deleteButton;

    private static final String CONFIRMATION_PHRASE = "Elimina il mio account SafeCore";
    private Runnable onCloseCallback;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void initialize() {
        if (emailLabel != null) {
            emailLabel.setText(SessionContext.getCurrentUserEmail());
        }

        // Abilita il pulsante di eliminazione solo se la frase è corretta
        if (confirmationField != null && deleteButton != null) {
            confirmationField.textProperty().addListener((obs, oldVal, newVal) -> {
                boolean isMatch = CONFIRMATION_PHRASE.equals(newVal);
                deleteButton.setDisable(!isMatch);

                if (isMatch) {
                    deleteButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: hand;");
                } else {
                    deleteButton.setStyle("-fx-background-color: #9ca3af; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-padding: 12; -fx-background-radius: 8; -fx-cursor: not-allowed;");
                }
            });
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Conferma Eliminazione Account");
        passwordDialog.setHeaderText("Inserisci la tua Master Password per confermare");

        ButtonType confirmButtonType = new ButtonType("Conferma Eliminazione", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Master Password");
        passwordField.setPrefWidth(300);

        passwordDialog.getDialogPane().setContent(passwordField);

        // Converti il risultato quando viene premuto il pulsante di conferma
        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        // Focus automatico sul campo password
        Platform.runLater(passwordField::requestFocus);

        Optional<String> result = passwordDialog.showAndWait();

        result.ifPresent(password -> {
            if (password == null || password.isBlank()) {
                showError("Password Richiesta", "Devi inserire la tua password per confermare l'eliminazione.");
                return;
            }

            try {
                String email = SessionContext.getCurrentUserEmail();
                userService.secureDeleteAccount(email, password);

                // Logout e redirect al login
                SessionContext.logout();

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Account Eliminato");
                successAlert.setHeaderText("Il tuo account è stato eliminato con successo");
                successAlert.setContentText("Tutti i tuoi dati sono stati rimossi in modo sicuro e permanente.");
                successAlert.showAndWait();

                Stage currentStage = (Stage) deleteButton.getScene().getWindow();
                SceneNavigator.switchTo(currentStage, "/com/safecore/ui/view/login.fxml", "Login");

            } catch (InvalidTokenException e) {
                showError("Password Errata", "La password inserita non è corretta. Impossibile eliminare l'account.");
            } catch (UserNotFoundException e) {
                showError("Errore", "Utente non trovato. Riprova.");
            } catch (Exception e) {
                showError("Errore", "Si è verificato un errore durante l'eliminazione dell'account: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        } else {
            Stage stage = (Stage) emailLabel.getScene().getWindow();
            stage.close();
        }
    }
}

