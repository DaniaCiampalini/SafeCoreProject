package com.safecore.ui.controller;

import com.safecore.business.service.*;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import com.safecore.business.domain.AuditResult;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class DashboardController implements VaultObserver {

    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private final BackupService backupService;
    private final SecurityAuditService auditService;
    private final SafeSendService safeSendService;

    // --- FXML FIELDS (CORRECT & COMPLETE) ---
    @FXML private Label userLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;

    @FXML private Region overlay;
    @FXML private VBox addEntryCard, generatePasswordCard, safeSendOverlayCard;

    @FXML private TextField newServiceField, newUsernameField, generatedPasswordField, safeSendIdField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextArea safeSendTextArea, safeSendResultArea;
    @FXML private CheckBox oneTimeCheckBox; // Reintegrato
    @FXML private ComboBox<Integer> expiryComboBox;

    @FXML private Label healthScoreLabel, auditDetailLabel;

    private final ObservableList<PasswordEntryEntity> masterData = FXCollections.observableArrayList();

    public DashboardController(PasswordGenerator passwordGenerator,
                               VaultService vaultService,
                               ApplicationContext applicationContext,
                               BackupService backupService,
                               SecurityAuditService auditService,
                               SafeSendService safeSendService) {
        this.passwordGenerator = passwordGenerator;
        this.vaultService = vaultService;
        this.applicationContext = applicationContext;
        this.backupService = backupService;
        this.auditService = auditService;
        this.safeSendService = safeSendService;
    }

    @FXML
    private void initialize() {
        userLabel.setText(SessionContext.getCurrentUserEmail());

        // Setup Tabella
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        setupActionsColumn();
        setupSearchFilter();
        setupExpiryOptions();

        // Sync iniziale
        vaultService.cleanupExpiredEntries();
        vaultService.addObserver(this);
        refreshVault();
    }

    // --- LOGICA VAULT ---

    private void setupSearchFilter() {
        FilteredList<PasswordEntryEntity> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(entry -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String filter = newVal.toLowerCase();
                return entry.getServiceName().toLowerCase().contains(filter) ||
                        entry.getUsername().toLowerCase().contains(filter);
            });
        });
        passwordTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button showBtn = new Button("👁");
            private final Button copyBtn = new Button("Copia");
            private final Button deleteBtn = new Button("Elimina");
            private final HBox container = new HBox(8, showBtn, copyBtn, deleteBtn);

            {
                showBtn.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-font-size: 16px;");
                copyBtn.setStyle("-fx-cursor: hand; -fx-background-color: #f3f4f6;");
                deleteBtn.setStyle("-fx-cursor: hand; -fx-background-color: #fee2e2; -fx-text-fill: #ef4444;");

                showBtn.setOnAction(e -> showPasswordDetails(getTableRow().getItem()));
                copyBtn.setOnAction(e -> {
                    PasswordEntryEntity entry = getTableRow().getItem();
                    if (entry != null) {
                        copyToClipboard(vaultService.decryptPassword(entry.getEncryptedPassword()));
                        showToast("Password copiata!");
                    }
                });
                deleteBtn.setOnAction(e -> {
                    PasswordEntryEntity entry = getTableRow().getItem();
                    if (entry != null) {
                        vaultService.deleteEntry(entry.getId());
                        refreshVault();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    @FXML
    private void handleConfirmSave() {
        String service = newServiceField.getText();
        String user = newUsernameField.getText();
        String pass = newPasswordField.getText();
        Integer expiryHours = expiryComboBox.getValue();

        if (service == null || service.isBlank() || pass == null || pass.isBlank()) {
            showToast("Dati mancanti!");
            return;
        }

        LocalDateTime expiresAt = (expiryHours != null && expiryHours > 0)
                ? LocalDateTime.now().plusHours(expiryHours) : null;

        vaultService.addEntry(service, user, pass, expiresAt);
        handleCloseOverlay();
        showToast("Salvato con successo!");
    }

    // --- LOGICA SAFESEND ---

    @FXML
    private void handleNewSafeSend() {
        handleCloseOverlay(); // Chiude altri eventuali overlay
        safeSendTextArea.clear();
        safeSendIdField.clear();
        safeSendResultArea.clear();
        oneTimeCheckBox.setSelected(true);
        oneTimeCheckBox.setDisable(true); // Sempre monouso come da tua logica

        overlay.setVisible(true);
        safeSendOverlayCard.setVisible(true);
    }

    @FXML
    private void handleConfirmSafeSend() {
        String content = safeSendTextArea.getText();
        if (content == null || content.isBlank()) return;

        try {
            // Generazione link (scadenza di default 24h)
            String link = safeSendService.createSafeLink(content, 24);
            copyToClipboard(link);
            showToast("Link SafeSend copiato negli appunti!");
            safeSendTextArea.setText("LINK GENERATO:\n" + link);
        } catch (Exception e) {
            showToast("Errore: " + e.getMessage());
        }
    }

    @FXML
    private void handleAccessSafeSend() {
        String input = safeSendIdField.getText();
        if (input == null || input.isBlank()) return;

        try {
            String raw = input.trim();
            String afterSlash = raw.contains("/") ? raw.substring(raw.lastIndexOf("/") + 1) : raw;
            String[] parts = afterSlash.split("\\?t=");

            if (parts.length != 2) throw new IllegalArgumentException("Formato link non valido.");

            UUID id = UUID.fromString(parts[0]);
            String token = parts[1];

            String decrypted = safeSendService.accessSafeLink(id, token);
            safeSendResultArea.setText(decrypted);
            showToast("Messaggio decifrato!");
        } catch (Exception e) {
            safeSendResultArea.setText("ERRORE: " + e.getMessage());
            showToast("Impossibile accedere.");
        }
    }

    // --- SECURITY AUDIT ---

    private void updateHealthScore() {
        AuditResult result = auditService.runAudit();
        healthScoreLabel.setText(result.score() + "/100");

        // Styling dinamico
        if (result.score() >= 80) healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #15803d;");
        else if (result.score() >= 50) healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #d97706;");
        else healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #dc2626;");

        StringBuilder detail = new StringBuilder();
        if (result.reusedCount() > 0) detail.append("⚠ ").append(result.reusedCount()).append(" password riutilizzate\n");
        if (result.weakCount() > 0) detail.append("⚠ ").append(result.weakCount()).append(" password deboli\n");
        if (result.oldCount() > 0) detail.append("⚠ ").append(result.oldCount()).append(" password vecchie");

        auditDetailLabel.setText(detail.isEmpty() ? "Il tuo vault è sicuro." : detail.toString().trim());
    }

    @FXML
    private void handleFullAudit() {
        AuditResult result = auditService.runAudit();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Sicurezza");
        alert.setHeaderText("Analisi Approfondita del Vault");
        alert.setContentText(String.format(
                "Punteggio Salute: %d/100\n\n- Password riutilizzate: %d\n- Password deboli: %d\n- Vecchie (>1 anno): %d",
                result.score(), result.reusedCount(), result.weakCount(), result.oldCount()
        ));
        alert.showAndWait();
    }

    // --- UI UTILITIES ---

    @FXML
    private void handleAddEntry() {
        handleCloseOverlay();
        newServiceField.clear();
        newUsernameField.clear();
        newPasswordField.clear();
        expiryComboBox.setValue(0);
        overlay.setVisible(true);
        addEntryCard.setVisible(true);
    }

    @FXML
    private void handleCloseOverlay() {
        overlay.setVisible(false);
        addEntryCard.setVisible(false);
        generatePasswordCard.setVisible(false);
        safeSendOverlayCard.setVisible(false);
    }

    private void setupExpiryOptions() {
        expiryComboBox.setItems(FXCollections.observableArrayList(0, 1, 12, 24, 168));
        expiryComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer h) {
                if (h == null || h == 0) return "Nessuna scadenza";
                if (h == 1) return "1 ora";
                if (h < 24) return h + " ore";
                return (h / 24) + " giorno/i";
            }
            @Override public Integer fromString(String s) { return 0; }
        });
        expiryComboBox.setValue(0);
    }

    private void showPasswordDetails(PasswordEntryEntity selected) {
        if (selected == null) return;
        String decrypted = vaultService.decryptPassword(selected.getEncryptedPassword());

        // Logica password scaduta (6 mesi) reintegrata
        boolean isExpired = selected.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(6));
        String prefix = isExpired ? "ATTENZIONE: Password scaduta (più di 6 mesi)!\n\n" : "";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dettagli Credenziali");
        alert.setHeaderText(selected.getServiceName());
        alert.setContentText(prefix + "Username: " + selected.getUsername() + "\nPassword: " + decrypted);
        alert.showAndWait();
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        toastLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(2), toastLabel);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> toastLabel.setVisible(false));
        ft.play();
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    @Override
    public void onVaultChanged() {
        Platform.runLater(this::refreshVault);
    }

    private void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
        updateHealthScore();
    }

    @FXML
    private void handleLogout() {
        SessionContext.logout();
        SceneNavigator.switchTo((Stage) userLabel.getScene().getWindow(), "/com/safecore/ui/view/login.fxml", "Login");
    }

    @FXML
    private void handleBackup() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Salva Backup Vault");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SafeCore Backup (*.safecore)", "*.safecore"));
        fc.setInitialFileName("vault_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".safecore");

        File file = fc.showSaveDialog(overlay.getScene().getWindow());
        if (file != null) {
            try {
                vaultService.exportVaultAsEncryptedJson(file);
                showToast("Backup eseguito!");
            } catch (Exception e) {
                showToast("Errore backup: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleGeneratePassword() {
        handleCloseOverlay();
        generatedPasswordField.setText(passwordGenerator.generateSafe(16));
        overlay.setVisible(true);
        generatePasswordCard.setVisible(true);
    }

    @FXML
    private void handleConfirmCopy() {
        copyToClipboard(generatedPasswordField.getText());
        handleCloseOverlay();
        showToast("Password copiata!");
    }
}