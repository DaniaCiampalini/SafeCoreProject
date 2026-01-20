package com.safecore.ui.controller;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.service.SecurityAuditService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller per la visualizzazione dei risultati dell'audit di sicurezza.
 * Gestisce l'interfaccia utente per mostrare il punteggio di sicurezza
 * e i dettagli delle password deboli, riutilizzate o vecchie.
 */

@Component
@Scope("prototype") // Importante: crea un nuovo controller ogni volta
public class AuditController {

    private final SecurityAuditService auditService;

    @FXML private Label scoreLabel;
    @FXML private Label weakCountLabel;
    @FXML private Label reusedCountLabel;
    @FXML private Label oldCountLabel;

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

        scoreLabel.setText(String.valueOf(result.score()));
        weakCountLabel.setText(String.valueOf(result.weakCount()));
        reusedCountLabel.setText(String.valueOf(result.reusedCount()));
        oldCountLabel.setText(String.valueOf(result.oldCount()));

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