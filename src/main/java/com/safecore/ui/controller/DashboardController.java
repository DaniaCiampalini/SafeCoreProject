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
import javafx.fxml.FXMLLoader;
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

@Component
public class DashboardController implements VaultObserver {

    // Servizi iniettati (usiamo le interfacce)
    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private final BackupService backupService;
    private final SecurityAuditService auditService; // Usiamo l'interfaccia!
    private final SafeSendService safeSendService;

    // --- CAMPI FXML ---
    @FXML private Label userLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;

    // Overlay e Card
    @FXML private Region overlay;
    @FXML private VBox addEntryCard, generatePasswordCard, safeSendOverlayCard, auditOverlayCard;

    // Input vari
    @FXML private TextField newServiceField, newUsernameField, generatedPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<Integer> expiryComboBox;

    // Widget della Dashboard (Health Bar)
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

        // Setup colonne tabella
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        setupActionsColumn();
        setupSearchFilter();
        setupExpiryOptions();

        // Ci mettiamo in ascolto dei cambiamenti nel vault
        vaultService.addObserver(this);
        vaultService.cleanupExpiredEntries();
        refreshVault();
    }

    // --- LOGICA CORE DEL VAULT ---

    @FXML
    private void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
        updateHealthScore(); // Aggiorna i numeretti della salute in tempo reale
    }

    private void updateHealthScore() {
        // Calcola al volo lo stato di salute per la card della dashboard
        AuditResult result = auditService.runAudit();
        healthScoreLabel.setText(result.score() + "/100");

        if (result.score() >= 80) healthScoreLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: 800; -fx-font-size: 24;");
        else if (result.score() >= 50) healthScoreLabel.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 800; -fx-font-size: 24;");
        else healthScoreLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 800; -fx-font-size: 24;");

        auditDetailLabel.setText("SafeCore ha rilevato " + (result.weakCount() + result.reusedCount()) + " criticità.");
    }

    @FXML
    private void handleConfirmSave() {
        String service = newServiceField.getText();
        String user = newUsernameField.getText();
        String pass = newPasswordField.getText();
        Integer hours = expiryComboBox.getValue();

        if (service == null || service.isBlank() || pass == null || pass.isBlank()) {
            showToast("Mancano dati!");
            return;
        }

        LocalDateTime expiry = (hours != null && hours > 0) ? LocalDateTime.now().plusHours(hours) : null;
        vaultService.addEntry(service, user, pass, expiry);

        handleCloseOverlay();
        showToast("Password salvata!");
        refreshVault();
    }

    // --- GESTIONE OVERLAY  ---

    @FXML
    private void handleCloseOverlay() {
        // Chiude tutto ciò che è aperto
        overlay.setVisible(false);
        addEntryCard.setVisible(false);
        generatePasswordCard.setVisible(false);
        safeSendOverlayCard.setVisible(false);
        auditOverlayCard.setVisible(false);
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

    // --- CARICAMENTO DINAMICO MODULI ---

    @FXML
    private void handleNewSafeSend() {
        loadDynamicModule("/com/safecore/ui/view/safesend-view.fxml", safeSendOverlayCard);
    }

    @FXML
    private void handleFullAudit() {
        loadDynamicModule("/com/safecore/ui/view/audit-view.fxml", auditOverlayCard);
    }

    /**
     * Metodo helper per caricare le viste esterne senza ripetere codice
     */
    private void loadDynamicModule(String fxmlPath, VBox container) {
        try {
            container.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean); //per far funzionare Spring!

            VBox view = loader.load();
            container.getChildren().add(view);

            handleCloseOverlay();
            overlay.setVisible(true);
            container.setVisible(true);
        } catch (Exception e) {
            showToast("Errore caricamento modulo!");
            e.printStackTrace();
        }
    }

    // --- UTILS (BACKUP, LOGOUT, CLIPBOARD) ---

    @FXML
    private void handleBackup() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Backup del Vault");
        fc.setInitialFileName("vault_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".safecore");
        File file = fc.showSaveDialog(overlay.getScene().getWindow());

        if (file != null) {
            try {
                vaultService.exportVaultAsEncryptedJson(file);
                showToast("Backup completato!");
            } catch (Exception e) {
                showToast("Errore backup!");
            }
        }
    }

    @FXML
    private void handleLogout() {
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

    // Obbligatorio per VaultObserver
    @Override
    public void onVaultChanged() {
        Platform.runLater(this::refreshVault);
    }

    // Setup filtri e combo
    private void setupSearchFilter() {
        FilteredList<PasswordEntryEntity> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filteredData.setPredicate(entry -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase();
                return entry.getServiceName().toLowerCase().contains(lower) || entry.getUsername().toLowerCase().contains(lower);
            });
        });
        passwordTable.setItems(filteredData);
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button copyBtn = new Button("Copia");
            private final Button delBtn = new Button("Elimina");
            private final HBox box = new HBox(8, copyBtn, delBtn);
            {
                copyBtn.setOnAction(e -> {
                    PasswordEntryEntity ent = getTableRow().getItem();
                    if (ent != null) {
                        copyToClipboard(vaultService.decryptPassword(ent.getEncryptedPassword()));
                        showToast("Copiata!");
                    }
                });
                delBtn.setOnAction(e -> {
                    PasswordEntryEntity ent = getTableRow().getItem();
                    if (ent != null) {
                        vaultService.deleteEntry(ent.getId());
                        refreshVault();
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void setupExpiryOptions() {
        expiryComboBox.setItems(FXCollections.observableArrayList(0, 1, 24, 168));
        expiryComboBox.setValue(0);
        expiryComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Integer h) {
                if (h == 0) return "Mai";
                if (h == 1) return "1 Ora";
                return (h / 24) + " Giorni";
            }
            @Override public Integer fromString(String s) { return 0; }
        });
    }
}