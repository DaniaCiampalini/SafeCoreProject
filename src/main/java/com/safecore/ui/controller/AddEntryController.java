package com.safecore.ui.controller;

import com.safecore.business.service.VaultService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class AddEntryController {

    private final VaultService vaultService;

    @FXML private TextField serviceField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private boolean saved = false;

    public AddEntryController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @FXML
    private void handleSave() {
        String service = serviceField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (isInputInvalid(service, username, password)) {
            return;
        }

        // Chiamata al metodo a 3 parametri del VaultService
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
        if (serviceField.getScene() != null) {
            Stage stage = (Stage) serviceField.getScene().getWindow();
            stage.close();
        }
    }
}