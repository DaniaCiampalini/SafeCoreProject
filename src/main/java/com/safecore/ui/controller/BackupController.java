package com.safecore.ui.controller;

import com.safecore.business.service.BackupService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;

import static com.safecore.ui.GlobalExceptionHandler.showError;

/**
 * Gestore I/O per il backup del vault.
 * Si affida interamente a BackupService per la cifratura dei file.
 */

@Component
public class BackupController {

    @Autowired
    private BackupService backupService;

    @FXML
    public void onExportAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Backup Cifrato");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SafeCore Backup (*.safe)", "*.safe")
        );
        fileChooser.setInitialFileName("safecore_backup_" + LocalDate.now() + ".safe"); // Timestamp nel nome per evitare sovrascrizioni


        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try {
                // TODO: Aggiungere un dialog di conferma, l'import attuale sovrascrive/unisce senza preavviso.
                backupService.exportBackup(file);
                showNotification("Successo", "Backup esportato correttamente in: " + file.getName());
            } catch (Exception e) {
                showError("Errore di Esportazione", "Impossibile salvare il backup: " + e.getMessage());
            }
        }
    }

    @FXML
    public void onImportAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Backup da Ripristinare");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SafeCore Backup (*.safe)", "*.safe")
        );

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(window);

        if (file != null) {
            try {
                backupService.importBackup(file);
                showNotification("Ripristino Completato", "I dati sono stati importati correttamente nel Vault.");
            } catch (Exception e) {
                showError("Errore di Importazione", "Impossibile caricare il backup: " + e.getMessage());
            }
        }
    }

    /**
     * Implementazione semplice per mostrare un feedback all'utente.
     */
    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);   // Standard JavaFX alert
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}