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

/**
 * Controller principale della Dashboard SafeCore.
 * Gestisce l'interazione tra l'utente e i servizi core: Vault, Backup, Audit, SafeSend e Hinting.
 * Implementa il pattern Observer per reagire in tempo reale alle modifiche dei dati.
 */
@Component
public class DashboardController implements VaultObserver {

    // Servizi iniettati tramite Spring (Principio di Dependency Injection)
    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private final BackupService backupService;
    private final SecurityAuditService auditService;
    private final SafeSendService safeSendService;
    private final PasswordHintService passwordHintService;

    // --- CAMPI FXML (Componenti UI) ---
    @FXML private Label userLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;

    @FXML private Region overlay;
    @FXML private VBox addEntryCard, generatePasswordCard, safeSendOverlayCard, auditOverlayCard;

    @FXML private TextField newServiceField, newUsernameField, generatedPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private ComboBox<Integer> expiryComboBox;

    @FXML private Label healthScoreLabel, auditDetailLabel;

    // Lista osservabile per il data-binding con la tabella
    private final ObservableList<PasswordEntryEntity> masterData = FXCollections.observableArrayList();

    public DashboardController(PasswordGenerator passwordGenerator,
                               VaultService vaultService,
                               ApplicationContext applicationContext,
                               BackupService backupService,
                               SecurityAuditService auditService,
                               SafeSendService safeSendService,
                               PasswordHintService passwordHintService) {
        this.passwordGenerator = passwordGenerator;
        this.vaultService = vaultService;
        this.applicationContext = applicationContext;
        this.backupService = backupService;
        this.auditService = auditService;
        this.safeSendService = safeSendService;
        this.passwordHintService = passwordHintService;
    }

    /**
     * Inizializzazione del controller. Configura i binding della tabella e i listener.
     */
    @FXML
    private void initialize() {
        userLabel.setText(SessionContext.getCurrentUserEmail());

        // Setup colonne tabella
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        setupActionsColumn();
        setupSearchFilter();
        setupExpiryOptions();

        // Registrazione Observer
        vaultService.addObserver(this);

        // Manutenzione iniziale
        vaultService.cleanupExpiredEntries();
        refreshVault();
    }

    // --- LOGICA BUSINESS (Vault & Health) ---

    @FXML
    public void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
        updateHealthScore();
    }

    private void updateHealthScore() {
        AuditResult result = auditService.runAudit();
        healthScoreLabel.setText(result.score() + "/100");

        if (result.score() >= 80) healthScoreLabel.setStyle("-fx-text-fill: #15803d; -fx-font-weight: 800; -fx-font-size: 24;");
        else if (result.score() >= 50) healthScoreLabel.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 800; -fx-font-size: 24;");
        else healthScoreLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 800; -fx-font-size: 24;");

        auditDetailLabel.setText("Rilevate " + (result.weakCount() + result.reusedCount()) + " criticità.");
    }

    /**
     * Gestisce il salvataggio di una nuova entry nel vault.
     * Valuta la password tramite PasswordHintService e mostra un alert se il livello è WARNING.
     */
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

        // --- ANALISI SICUREZZA ---
        PasswordHint hint = passwordHintService.evaluatePassword(pass);

        if (hint.getLevel() == HintLevel.WARNING) {
            // Caso: Password debole - Chiediamo conferma
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sicurezza Password");
            alert.setHeaderText("Attenzione: Password Poco Sicura");
            alert.setContentText(hint.getMessage() + "\n\nVuoi salvarla comunque?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.NO) {
                return;
            }
        } else {
            // Caso: Password sicura - Feedback positivo
            showToast("✓ Password Forte!");
        }

        // --- SALVATAGGIO ---
        LocalDateTime expiry = (hours != null && hours > 0) ? LocalDateTime.now().plusHours(hours) : null;
        vaultService.addEntry(service, user, pass, expiry);

        handleCloseOverlay();
        // Il refresh avviene tramite onVaultChanged() dell'Observer
    }

    // --- OPERAZIONI DI BACKUP (ESPORTA & IMPORTA) ---

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
                showSuccessAlert("Importazione Completata", "Le password sono state integrate correttamente.");
            } catch (Exception e) {
                showError("Errore Importazione", "Il file selezionato non è valido o la chiave è errata.");
            }
        }
    }

    // --- GESTIONE INTERFACCIA E OVERLAY ---

    @FXML
    private void handleCloseOverlay() {
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

    @FXML
    private void handleNewSafeSend() {
        loadDynamicModule("/com/safecore/ui/view/safesend-view.fxml", safeSendOverlayCard);
    }

    @FXML
    private void handleFullAudit() {
        loadDynamicModule("/com/safecore/ui/view/audit-view.fxml", auditOverlayCard);
    }

    private void loadDynamicModule(String fxmlPath, VBox container) {
        try {
            container.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            VBox view = loader.load();
            container.getChildren().add(view);
            handleCloseOverlay();
            overlay.setVisible(true);
            container.setVisible(true);
        } catch (Exception e) {
            showToast("Errore modulo!");
            e.printStackTrace();
        }
    }

    // --- UTILS ---

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

    // --- SETUP UI ---

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
                    if (ent != null) {
                        vaultService.deleteEntry(ent.getId());
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
                if (h == 0) return "Nessuna Scadenza";
                if (h == 1) return "1 Ora";
                if (h == 24) return "1 Giorno";
                return (h / 24) + " Giorni";
            }
            @Override public Integer fromString(String s) { return 0; }
        });
    }
}