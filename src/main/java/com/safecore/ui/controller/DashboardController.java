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
import org.springframework.stereotype.Component;

/**
 * Dashboard principale SafeCore.
 * Adesso utilizza il PasswordGenerator iniettato da Spring.
 */
@Component
public class DashboardController {

    @FXML private Label userLabel;
    @FXML private Label securityStatusLabel;
    @FXML private Label toastLabel;
    @FXML private VBox passwordCard;
    @FXML private VBox vaultCard;
    @FXML private VBox backupCard;

    private final PasswordGenerator passwordGenerator;

    // Costruttore per la Dependency Injection
    public DashboardController(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @FXML
    private void initialize() {
        // Null check preventivo per evitare problemi se SessionContext è vuoto
        String email = SessionContext.getLoggedUserEmail() != null ? SessionContext.getLoggedUserEmail() : "User";
        userLabel.setText(email);
        securityStatusLabel.setText("Security status: OK");

        applyHoverAnimation(passwordCard);
        applyHoverAnimation(vaultCard);
        applyHoverAnimation(backupCard);
    }

    @FXML
    private void handleGeneratePassword() {
        // CORRETTO: Uso del metodo d'istanza generateSafe
        String pwd = passwordGenerator.generateSafe(16);
        showToast("Generated password: " + pwd);
    }

    @FXML
    private void handleLogout() {
        SessionContext.logout();
        Stage stage = (Stage) userLabel.getScene().getWindow();
        SceneNavigator.switchTo(stage, "/com/safecore/ui/view/login.fxml", "SafeCore – Login");
    }

    // ... handleOpenVault, handleBackup e metodi grafici restano uguali ...

    @FXML
    private void handleOpenVault() {
        showToast("Vault opened (Coming Soon)");
    }

    @FXML
    private void handleBackup() {
        showToast("Encrypted backup exported");
    }

    private void applyHoverAnimation(VBox card) {
        card.setStyle(baseCardStyle());
        card.setOnMouseEntered(e -> card.setStyle(baseCardStyle() + "-fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        card.setOnMouseExited(e -> card.setStyle(baseCardStyle()));
    }

    private String baseCardStyle() {
        return "-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; " +
                "-fx-border-color: #e5e7eb; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 4);";
    }

    private void showToast(String message) {
        toastLabel.setText(message);
        toastLabel.setVisible(true);
        FadeTransition fade = new FadeTransition(Duration.seconds(3), toastLabel);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> toastLabel.setVisible(false));
        fade.play();
    }
}