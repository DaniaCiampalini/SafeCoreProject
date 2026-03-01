package com.safecore.ui.controller;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;
import com.safecore.business.service.*;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.safecore.ui.GlobalExceptionHandler.showError;

@Component
public class DashboardController implements VaultObserver {

    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private final BackupService backupService;
    private final SecurityAuditService auditService;
    private final SafeSendService safeSendService;
    private final PasswordHintService passwordHintService;
    private final UserService userService;

    @FXML private Label userLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;

    @FXML private Region overlay;
    @FXML private VBox addEntryCard, generatePasswordCard, safeSendOverlayCard, auditOverlayCard, settingsOverlayCard;

    @FXML private TextField newServiceField, newUsernameField, generatedPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<Integer> expiryComboBox;

    @FXML private Label healthScoreLabel, auditDetailLabel;

    // Lista master sincronizzata col DB tramite VaultService
    private final ObservableList<PasswordEntryEntity> masterData = FXCollections.observableArrayList();

    public DashboardController(PasswordGenerator passwordGenerator,
                               VaultService vaultService,
                               ApplicationContext applicationContext,
                               BackupService backupService,
                               SecurityAuditService auditService,
                               SafeSendService safeSendService,
                               PasswordHintService passwordHintService,
                               UserService userService) {
        this.passwordGenerator = passwordGenerator;
        this.vaultService = vaultService;
        this.applicationContext = applicationContext;
        this.backupService = backupService;
        this.auditService = auditService;
        this.safeSendService = safeSendService;
        this.passwordHintService = passwordHintService;
        this.userService = userService;
    }

    @FXML
    private void initialize() {
        if (userLabel != null) {
            userLabel.setText(SessionContext.getCurrentUserEmail());
        }

        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        setupActionsColumn();
        setupSearchFilter();
        setupExpiryOptions();

        vaultService.addObserver(this);
        vaultService.cleanupExpiredEntries();
        refreshVault();
    }

    @FXML
    public void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
        updateHealthScore();
    }

    private void updateHealthScore() {
        AuditResult result = auditService.runAudit();
        if (healthScoreLabel != null) {
            healthScoreLabel.setText(result.score() + "/100");

            if (result.score() >= 80) healthScoreLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: 800; -fx-font-size: 24;");
            else if (result.score() >= 50) healthScoreLabel.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 800; -fx-font-size: 24;");
            else healthScoreLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 800; -fx-font-size: 24;");
        }

        if (auditDetailLabel != null) {
            auditDetailLabel.setText("Rilevate " + (result.weakCount() + result.reusedCount()) + " criticità.");
        }
    }

    @FXML
    private void handleConfirmSave() {
        String service = newServiceField.getText();
        String user = newUsernameField.getText();
        String pass = newPasswordField.getText();
        Integer hours = expiryComboBox.getValue();

        if (service == null || service.isBlank() || pass == null || pass.isBlank()) {
            showToast("Dati incompleti!");
            return;
        }

        PasswordHint hint = passwordHintService.evaluatePassword(pass);
        if (hint.getLevel() == HintLevel.WARNING) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sicurezza Password");
            alert.setHeaderText("Attenzione: Password Poco Sicura");
            alert.setContentText(hint.getMessage() + "\n\nVuoi salvarla comunque?");
            alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

            // Imposta NO come pulsante di default (blu) per motivi di sicurezza
            Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
            noButton.setDefaultButton(true);
            Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
            yesButton.setDefaultButton(false);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.NO) {
                handleCloseOverlay(); // Chiudi l'overlay e torna alla dashboard
                return;
            }
        }
        if (hint.getLevel() == HintLevel.INFO) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sicurezza Password");
            alert.setHeaderText("Attenzione: Password Poco Sicura");
            alert.setContentText(hint.getMessage() + "\n\nVuoi salvarla comunque?");
            alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

            // Imposta NO come pulsante di default (blu) per motivi di sicurezza
            Button noButton = (Button) alert.getDialogPane().lookupButton(ButtonType.NO);
            noButton.setDefaultButton(true);
            Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.YES);
            yesButton.setDefaultButton(false);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isEmpty() || result.get() == ButtonType.NO) {
                handleCloseOverlay(); // Chiudi l'overlay e torna alla dashboard
                return;
            }
        }

        LocalDateTime expiry = (hours != null && hours > 0) ? LocalDateTime.now().plusHours(hours) : null;
        vaultService.addEntry(service, user, pass, expiry);
        handleCloseOverlay();
    }

    @FXML
    private void handleBackup(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Esporta Vault Cifrato");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SafeCore Backup (*.safe)", "*.safe"));
        fc.setInitialFileName("safecore_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".safe");

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fc.showSaveDialog(window);

        if (file != null) {
            try {
                backupService.exportBackup(file);
                showSuccessAlert("Esportazione Completata", "File cifrato salvato in: " + file.getName());
            } catch (Exception e) {
                showError("Errore Backup", "Esportazione fallita: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleImport(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importa Vault");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SafeCore Backup (*.safe)", "*.safe"));

        Window window = ((Node) event.getSource()).getScene().getWindow();
        File file = fc.showOpenDialog(window);

        if (file != null) {
            try {
                backupService.importBackup(file);
                refreshVault();
                showSuccessAlert("Importazione Completata", "Le password sono state integrate correttamente.");
            } catch (Exception e) {
                showError("Errore Importazione", "Il file selezionato non è valido o la chiave è errata.");
            }
        }
    }

    @FXML
    private void handleCloseOverlay() {
        overlay.setVisible(false);
        addEntryCard.setVisible(false);
        generatePasswordCard.setVisible(false);
        safeSendOverlayCard.setVisible(false);
        auditOverlayCard.setVisible(false);
        settingsOverlayCard.setVisible(false);
    }

    @FXML
    private void handleAddEntry() {
        handleCloseOverlay();
        newServiceField.clear();
        newUsernameField.clear();
        newPasswordField.clear();
        overlay.setVisible(true);
        addEntryCard.setVisible(true);
    }

    @FXML
    private void handleGeneratePassword() {
        handleCloseOverlay();
        generatedPasswordField.setText(passwordGenerator.generateSafe(16));
        overlay.setVisible(true);
        generatePasswordCard.setVisible(true);
    }

    @FXML
    private void handleNewSafeSend() {
        loadDynamicModule("/com/safecore/ui/view/safesend-view.fxml", safeSendOverlayCard);
    }

    @FXML
    private void handleFullAudit() {
        loadDynamicModule("/com/safecore/ui/view/audit-view.fxml", auditOverlayCard);
    }

    @FXML
    private void handleSettings() {
        loadSettingsModule("/com/safecore/ui/view/settings-view.fxml", settingsOverlayCard);
    }

    private void loadSettingsModule(String fxmlPath, VBox container) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            VBox view = loader.load();

            // Ottieni il controller e imposta la callback di chiusura
            SettingsController controller = loader.getController();
            if (controller != null) {
                controller.setOnCloseCallback(this::handleCloseOverlay);
            }

            Platform.runLater(() -> {
                handleCloseOverlay();
                container.getChildren().setAll(view);
                overlay.setVisible(true);
                container.setVisible(true);
            });
        } catch (Exception e) {
            showToast("Errore caricamento impostazioni");
            e.printStackTrace();
        }
    }

    private void loadDynamicModule(String fxmlPath, VBox container) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            VBox view = loader.load();

            Platform.runLater(() -> {
                handleCloseOverlay();
                container.getChildren().setAll(view);
                overlay.setVisible(true);
                container.setVisible(true);
            });
        } catch (Exception e) {
            showToast("Errore caricamento modulo");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        userService.logout();
        SessionContext.logout();
        SceneNavigator.switchTo((Stage) userLabel.getScene().getWindow(), "/com/safecore/ui/view/login.fxml", "Login");
    }

    @FXML
    private void handleConfirmCopy() {
        copyToClipboard(generatedPasswordField.getText());
        handleCloseOverlay();
        showToast("Copiata!");
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        toastLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(1.5), toastLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> toastLabel.setVisible(false));
        ft.play();
    }

    private void showSuccessAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @Override
    public void onVaultChanged() {
        Platform.runLater(this::refreshVault);
    }

    private void setupSearchFilter() {
        FilteredList<PasswordEntryEntity> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(entry -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return entry.getServiceName().toLowerCase().contains(lower) ||
                        entry.getUsername().toLowerCase().contains(lower);
            });
        });
        passwordTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button copyBtn = new Button("Copia");
            private final Button delBtn = new Button("Elimina");
            private final HBox box = new HBox(10, copyBtn, delBtn);
            {
                copyBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;");

                copyBtn.setOnAction(e -> {
                    PasswordEntryEntity ent = getTableRow().getItem();
                    if (ent != null) {
                        copyToClipboard(vaultService.decryptPassword(ent.getEncryptedPassword()));
                        showToast("Copiata!");
                    }
                });
                delBtn.setOnAction(e -> {
                    PasswordEntryEntity ent = getTableRow().getItem();
                    if (ent != null) vaultService.deleteEntry(ent.getId());
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupExpiryOptions() {
        expiryComboBox.setItems(FXCollections.observableArrayList(0, 1, 12, 24, 48));
        expiryComboBox.setValue(0);
        expiryComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer h) {
                if (h == null || h == 0) return "Nessuna Scadenza";
                if (h == 1) return "1 Ora";
                if (h == 12) return "12 Ore";
                if (h == 24) return "1 Giorno";
                return (h / 24) + " Giorni";
            }
            @Override public Integer fromString(String s) { return 0; }
        });
    }
}