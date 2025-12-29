package com.safecore.ui.controller;

import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Dashboard post-login.
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private void initialize() {

        // blocco accesso diretto
        if (!UserSession.isLoggedIn()) {
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            SceneNavigator.switchTo(
                    stage,
                    "/login.fxml",
                    "SafeCore – Login"
            );
            return;
        }

        welcomeLabel.setText(
                "Welcome, " + UserSession.getUserEmail()
        );
    }

    @FXML
    private void handleLogout() {
        UserSession.logout();

        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        SceneNavigator.switchTo(
                stage,
                "/login.fxml",
                "SafeCore – Login"
        );
    }
}

