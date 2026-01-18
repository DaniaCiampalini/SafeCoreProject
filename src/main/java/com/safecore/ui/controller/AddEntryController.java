package com.safecore.ui.controller;

import com.safecore.business.service.VaultService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/**
 * Gestore del popup per l'inserimento manuale delle credenziali.
 */
@Component
public class AddEntryController {

    private final VaultService vaultService;

    @FXML private TextField serviceField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    // Flag per rinfrescare la tabella solo se necessario
    private boolean saved = false;

    public AddEntryController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @FXML
    private void handleSave() {
        String service = serviceField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();   // TODO: trim() sulle stringhe per spazi extra?

        if (isInputInvalid(service, username, password)) {
            return;
        }

        // Delega la crittografia e il salvataggio al service, il controller è UI-only
        vaultService.addEntry(service, username, password);

        saved = true;
        closeStage();
    }

    private boolean isInputInvalid(String s, String u, String p) {
        return s == null || s.isBlank() || u == null || u.isBlank() || p == null || p.isBlank();
    }

    @FXML
    private void handleCancel() {
        closeStage();
    }

    public boolean isSaved() {
        return saved;
    }

    private void closeStage() {
        // Per evitare di iniettare lo Stage nel costruttore
        if (serviceField.getScene() != null) {
            Stage stage = (Stage) serviceField.getScene().getWindow();
            stage.close();
        }
    }
}