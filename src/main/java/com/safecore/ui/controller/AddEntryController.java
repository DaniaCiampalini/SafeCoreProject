package com.safecore.ui.controller;

import com.safecore.business.service.VaultService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

/**
 * Controller per la piccola finestra popup che serve ad aggiungere una nuova password.
 * È una versione semplificata di quello che succede nella Dashboard, usata magari
 * in contesti dove serve solo l'inserimento rapido.
 */
@Component
public class AddEntryController {

    private final VaultService vaultService;
    @FXML
    private TextField serviceField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    private boolean saved = false;

    public AddEntryController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    /**
     * Salva i dati inseriti nel vault.
     */
    @FXML
    private void handleSave() {
        String service = serviceField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Controlliamo che l'utente non stia salvando roba vuota
        if (service == null || service.isBlank() || username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }

        // Passiamo tutto al VaultService. Sarà lui a:
        // 1. Cifrare la password con AES
        // 2. Capire a quale utente appartiene (tramite la sessione)
        // 3. Salvare tutto sul DB
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
