package com.safecore.ui.controller;

import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller della Dashboard principale.
 *
 * Responsabilità:
 * - Visualizzazione info utente
 * - Entry point post-login
 * - Logout
 *
 * Nessuna logica di business.
 */
public class DashboardController {

    @FXML
    private Label userLabel;

    @FXML
    private Label securityStatusLabel;

    @FXML
    private void initialize() {

        if (!SessionContext.isLoggedIn()) {
            userLabel.setText("Unknown user");
            securityStatusLabel.setText("Security status: unknown");
            return;
        }

        userLabel.setText(SessionContext.getLoggedUserEmail());
        securityStatusLabel.setText("Security status: OK");
    }

    @FXML
    private void handleLogout() {

        SessionContext.logout();

        Stage stage = (Stage) userLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/login.fxml", "SafeCore – Login");
    }
}
