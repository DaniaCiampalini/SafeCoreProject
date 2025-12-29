package com.safecore.ui.controller;

import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controller della Dashboard.
 *
 * Responsabilità:
 * - UI post-login
 * - Accesso allo stato di sessione
 * - Logout
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label securityStatusLabel;

    @FXML
    private void initialize() {

        if (!SessionContext.isLoggedIn()) {
            // sicurezza extra
            welcomeLabel.setText("Unknown user");
            return;
        }

        welcomeLabel.setText(
                "Welcome, " + SessionContext.getLoggedUserEmail()
        );

        securityStatusLabel.setText("Security status: OK");
    }

    @FXML
    private void handleLogout() {

        SessionContext.logout();

        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        SceneNavigator.switchTo(
                stage,
                "/login.fxml",
                "SafeCore – Login"
        );
    }
}

