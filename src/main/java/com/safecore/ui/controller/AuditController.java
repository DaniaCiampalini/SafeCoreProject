package com.safecore.ui.controller;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.service.SecurityAuditService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller per la visualizzazione dei risultati dell'audit di sicurezza.
 * Gestisce l'interfaccia utente per mostrare il punteggio di sicurezza
 * e i dettagli delle password deboli, riutilizzate o vecchie con i servizi coinvolti.
 */

@Component
@Scope("prototype") // Importante: crea un nuovo controller ogni volta
public class AuditController {

    private final SecurityAuditService auditService;

    @FXML private Label scoreLabel;
    @FXML private Label weakCountLabel;
    @FXML private Label reusedCountLabel;
    @FXML private Label oldCountLabel;
    @FXML private TextArea weakServicesArea;
    @FXML private TextArea reusedServicesArea;
    @FXML private TextArea oldServicesArea;

    public AuditController(SecurityAuditService auditService) {
        this.auditService = auditService;
    }

    @FXML
    public void initialize() {
        if (scoreLabel != null && auditService != null) {
            refreshAudit();
        }
    }

    public void refreshAudit() {
        AuditResult result = auditService.runAudit();

        // Mostra il punteggio con 1 decimale
        scoreLabel.setText(String.format("%.1f", result.score()));
        weakCountLabel.setText(String.valueOf(result.weakCount()));
        reusedCountLabel.setText(String.valueOf(result.reusedCount()));
        oldCountLabel.setText(String.valueOf(result.oldCount()));

        // Mostra i servizi con password deboli
        if (weakServicesArea != null) {
            if (result.weakPasswordServices().isEmpty()) {
                weakServicesArea.setText("Nessuna password debole rilevata");
                weakServicesArea.setStyle("-fx-text-fill: #15803d;");
            } else {
                String services = String.join("\n• ", result.weakPasswordServices());
                weakServicesArea.setText("• " + services);
                weakServicesArea.setStyle("-fx-text-fill: #dc2626;");
            }
        }

        // Mostra i servizi con password replicate
        if (reusedServicesArea != null) {
            if (result.reusedPasswordServices().isEmpty()) {
                reusedServicesArea.setText("Nessuna password replicata");
                reusedServicesArea.setStyle("-fx-text-fill: #15803d;");
            } else {
                String services = String.join("\n• ", result.reusedPasswordServices());
                reusedServicesArea.setText("• " + services);
                reusedServicesArea.setStyle("-fx-text-fill: #d97706;");
            }
        }

        // Mostra i servizi con password vecchie
        if (oldServicesArea != null) {
            if (result.oldPasswordServices().isEmpty()) {
                oldServicesArea.setText("Nessuna password vecchia (>1 anno)");
                oldServicesArea.setStyle("-fx-text-fill: #15803d;");
            } else {
                String services = String.join("\n• ", result.oldPasswordServices());
                oldServicesArea.setText("• " + services);
                oldServicesArea.setStyle("-fx-text-fill: #d97706;");
            }
        }

        // Colora il punteggio
        if (result.score() >= 80) scoreLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 24;");
        else if (result.score() >= 50) scoreLabel.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-font-size: 24;");
        else scoreLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 24;");
    }

    @FXML
    private void handleClose() {
        scoreLabel.getScene().lookup("#overlay").setVisible(false);
        scoreLabel.getScene().lookup("#auditOverlayCard").setVisible(false);
    }
}