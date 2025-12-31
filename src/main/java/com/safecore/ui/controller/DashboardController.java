package com.safecore.ui.controller;

import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Dashboard principale SafeCore.
 *
 * Stato: FINALE.
 */
public class DashboardController {

    @FXML
    private Label userLabel;

    @FXML
    private Label securityStatusLabel;

    @FXML
    private Label toastLabel;

    @FXML
    private VBox passwordCard;

    @FXML
    private VBox vaultCard;

    @FXML
    private VBox backupCard;

    @FXML
    private void initialize() {

        userLabel.setText(SessionContext.getLoggedUserEmail());
        securityStatusLabel.setText("Security status: OK");

        applyHoverAnimation(passwordCard);
        applyHoverAnimation(vaultCard);
        applyHoverAnimation(backupCard);
    }

    // ---------------- DASHBOARD ACTIONS ----------------

    @FXML
    private void handleGeneratePassword() {
        String pwd = PasswordGenerator.generate(16);
        showToast("Generated password: " + pwd);
    }

    @FXML
    private void handleOpenVault() {
        showToast("Vault opened (mock)");
    }

    @FXML
    private void handleBackup() {
        showToast("Encrypted backup exported");
    }

    @FXML
    private void handleLogout() {
        SessionContext.logout();
        Stage stage = (Stage) userLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/login.fxml", "SafeCore – Login");
    }

    // ---------------- UI HELPERS ----------------

    private void applyHoverAnimation(VBox card) {
        card.setStyle(baseCardStyle());

        card.setOnMouseEntered(e ->
                card.setStyle(baseCardStyle() + "-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));

        card.setOnMouseExited(e ->
                card.setStyle(baseCardStyle()));
    }

    private String baseCardStyle() {
        return "-fx-background-color: white;" +
               "-fx-background-radius: 12;" +
               "-fx-border-radius: 12;" +
               "-fx-border-color: #e5e7eb;" +
               "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 4);";
    }

    private void showToast(String message) {

        toastLabel.setText(message);
        toastLabel.setOpacity(1);
        toastLabel.setVisible(true);

        FadeTransition fade = new FadeTransition(Duration.seconds(3), toastLabel);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> toastLabel.setVisible(false));
        fade.play();
    }
}

