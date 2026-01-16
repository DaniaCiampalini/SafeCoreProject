package com.safecore.ui.controller;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.service.SecurityAuditService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

@Component
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
        refreshAudit();
    }

    public void refreshAudit() {
        AuditResult result = auditService.runAudit();

        scoreLabel.setText(String.valueOf(result.score()));
        weakCountLabel.setText(String.valueOf(result.weakCount()));
        reusedCountLabel.setText(String.valueOf(result.reusedCount()));
        oldCountLabel.setText(String.valueOf(result.oldCount()));

        // Cambio colore dinamico basato sul punteggio
        if (result.score() >= 80) scoreLabel.setStyle("-fx-text-fill: #15803d; -fx-font-size: 24; -fx-font-weight: bold;");
        else if (result.score() >= 50) scoreLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 24; -fx-font-weight: bold;");
        else scoreLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 24; -fx-font-weight: bold;");
    }

    @FXML
    private void handleClose() {
        // Logica per chiudere l'overlay (verrà gestita tramite il DashboardController o un evento)
        scoreLabel.getScene().lookup("#overlay").setVisible(false);
        scoreLabel.getScene().lookup("#auditOverlayCard").setVisible(false);
    }
}