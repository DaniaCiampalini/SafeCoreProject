package com.safecore.ui.controller;

import com.safecore.business.service.SafeSendService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SafeSendController {

    private final SafeSendService safeSendService;

    @FXML private TextArea safeSendTextArea;
    @FXML private TextField safeSendIdField;
    @FXML private TextArea safeSendResultArea;
    @FXML private ComboBox<Integer> safeSendExpiryCombo;

    public SafeSendController(SafeSendService safeSendService) {
        this.safeSendService = safeSendService;
    }

    @FXML
    public void initialize() {
        // Popoliamo le opzioni di scadenza (1h, 12h, 24h, 1 settimana)
        if (safeSendExpiryCombo != null) {
            safeSendExpiryCombo.getItems().addAll(1, 12, 24, 168);
            safeSendExpiryCombo.setValue(24);
        }
    }

    @FXML
    private void handleConfirmSafeSend() {
        String content = safeSendTextArea.getText();
        if (content == null || content.isBlank()) return;

        try {
            int hours = safeSendExpiryCombo.getValue();
            String link = safeSendService.createSafeLink(content, hours);

            copyToClipboard(link);
            safeSendTextArea.setText("LINK GENERATO E COPIATO:\n" + link);
        } catch (Exception e) {
            showError("Errore generazione: " + e.getMessage());
        }
    }

    @FXML
    private void handleAccessSafeSend() {
        String input = safeSendIdField.getText();
        if (input == null || input.isBlank()) return;

        try {
            // Logica di parsing del link per estrarre ID e Token
            String raw = input.trim();
            String afterSlash = raw.contains("/") ? raw.substring(raw.lastIndexOf("/") + 1) : raw;
            String[] parts = afterSlash.split("\\?t=");

            if (parts.length != 2) throw new IllegalArgumentException("Formato link non valido.");

            UUID id = UUID.fromString(parts[0]);
            String token = parts[1];

            String decrypted = safeSendService.accessSafeLink(id, token);
            safeSendResultArea.setText(decrypted);
        } catch (Exception e) {
            safeSendResultArea.setText("ERRORE: " + e.getMessage());
        }
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(message);
        alert.showAndWait();
    }
}