package com.safecore.ui.controller;

import com.safecore.business.service.VaultService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class AddEntryController {

    @FXML private TextField serviceField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private final VaultService vaultService;
    private boolean saved = false;

    public AddEntryController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @FXML
    private void handleSave() {
        String service = serviceField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (service == null || service.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            // Qui si potrebbe aggiungere un alert di errore
            return;
        }

        // VaultService si occupa di: cifratura on-the-fly e associazione utente tramite SessionContext
        vaultService.addEntry(service, username, password);
        
        saved = true;
        closeStage();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    public boolean isSaved() {
        return saved;
    }

    private void closeStage() {
        Stage stage = (Stage) serviceField.getScene().getWindow();
        stage.close();
    }
}
