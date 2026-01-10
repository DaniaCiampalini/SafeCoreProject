package com.safecore.ui.controller;

import com.safecore.business.service.*;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordGenerator;
import com.safecore.ui.navigation.SceneNavigator;
import com.safecore.ui.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.springframework.context.ApplicationContext;
import javafx.scene.layout.Region;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DashboardController implements VaultObserver {

    // Tutte queste sono le "collegamenti" con il file FXML (la grafica)
    @FXML private Label userLabel, securityStatusLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;
    @FXML private VBox passwordCard, vaultCard, backupCard, auditCard, safeSendCard, aliasCard;

    // Componenti per l'interfaccia degli Overlay (i pannelli che appaiono sopra)
    @FXML private Region overlay;
    @FXML private VBox addEntryCard, generatePasswordCard, safeSendOverlayCard, aliasOverlayCard;
    @FXML private TextField newServiceField, newUsernameField, generatedPasswordField, aliasServiceField, safeSendIdField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextArea safeSendTextArea, safeSendResultArea;
    @FXML private CheckBox oneTimeCheckBox;
    @FXML private ComboBox<Integer> expiryComboBox;
    @FXML private Label healthScoreLabel, auditDetailLabel;
    @FXML private ListView<String> aliasListView;

    // Qui iniettiamo tutti i servizi che ci servono per far funzionare la logica
    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private final BackupService backupService;
    private final SecurityAuditService auditService;
    private final SafeSendService safeSendService;
    private final EmailAliasService emailAliasService;
    private final AutoFillService autoFillService;
    
    // Lista "viva" che tiene i dati della tabella e si aggiorna da sola
    private ObservableList<PasswordEntryEntity> masterData = FXCollections.observableArrayList();

    public DashboardController(PasswordGenerator passwordGenerator,
                               VaultService vaultService,
                               ApplicationContext applicationContext,
                               BackupService backupService,
                               SecurityAuditService auditService,
                               SafeSendService safeSendService,
                               EmailAliasService emailAliasService,
                               AutoFillService autoFillService) {
        this.passwordGenerator = passwordGenerator;
        this.vaultService = vaultService;
        this.applicationContext = applicationContext;
        this.backupService = backupService;
        this.auditService = auditService;
        this.safeSendService = safeSendService;
        this.emailAliasService = emailAliasService;
        this.autoFillService = autoFillService;
    }

    @FXML
    private void initialize() {
        // Appena la dashboard si apre, mettiamo l'email dell'utente in alto
        userLabel.setText(SessionContext.getCurrentUserEmail());

        // Diciamo alla tabella quali campi dell'oggetto PasswordEntryEntity mostrare
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        // Setup vari: pulsanti, filtri di ricerca e scadenze
        setupActionsColumn();
        setupSearchFilter();
        setupExpiryOptions();

        // Pulizia automatica delle password scadute e registrazione come "osservatore"
        // Così se il Vault cambia, questa classe viene avvisata e aggiorna la tabella.
        vaultService.cleanupExpiredEntries(); 
        vaultService.addObserver(this);
        refreshVault();

        // Un tocco di classe: animazioni quando passi il mouse sulle card
        applyHoverAnimation(passwordCard);
        applyHoverAnimation(vaultCard);
        applyHoverAnimation(backupCard);
        applyHoverAnimation(auditCard);
        applyHoverAnimation(safeSendCard);
        applyHoverAnimation(aliasCard);
    }

    private void setupExpiryOptions() {
        expiryComboBox.setItems(FXCollections.observableArrayList(0, 1, 12, 24, 168)); // 0 = mai, 1, 12, 24 ore, 168 = 1 settimana
        expiryComboBox.setConverter(new StringConverter<Integer>() {
            @Override
            public String toString(Integer hours) {
                if (hours == null || hours == 0) return "Nessuna scadenza";
                if (hours == 1) return "1 ora";
                if (hours < 24) return hours + " ore";
                if (hours == 24) return "1 giorno";
                if (hours == 168) return "1 settimana";
                return hours + " ore";
            }

            @Override
            public Integer fromString(String string) {
                return 0;
            }
        });
        expiryComboBox.setValue(0);
    }

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
            private final Button autoFillBtn = new Button("⚡");
            private final Button copyBtn = new Button("Copia");
            private final Button deleteBtn = new Button("Elimina");
            private final HBox container = new HBox(8, showBtn, autoFillBtn, copyBtn, deleteBtn);

            {
                showBtn.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-font-size: 16px;");
                autoFillBtn.setTooltip(new Tooltip("Auto-Digitazione (2s per cambiare finestra)"));
                autoFillBtn.setStyle("-fx-cursor: hand; -fx-background-color: #fef3c7; -fx-text-fill: #d97706;");
                copyBtn.setStyle("-fx-cursor: hand; -fx-background-color: #f3f4f6;");
                deleteBtn.setStyle("-fx-cursor: hand; -fx-background-color: #fee2e2; -fx-text-fill: #ef4444;");

                showBtn.setOnAction(e -> {
                    PasswordEntryEntity entry = getTableRow().getItem();
                    if (entry != null) {
                        showPasswordDetails(entry);
                    }
                });

                autoFillBtn.setOnAction(e -> {
                    PasswordEntryEntity entry = getTableRow().getItem();
                    if (entry != null) {
                        showToast("Passa al sito... digitazione tra 2 secondi!");
                        String pass = vaultService.decryptPassword(entry.getEncryptedPassword());
                        new Thread(() -> autoFillService.typeCredentials(entry.getUsername(), pass)).start();
                    }
                });

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

    private void showPasswordDetails(PasswordEntryEntity selected) {
        String decrypted = vaultService.decryptPassword(selected.getEncryptedPassword());
        
        // Logica Scadenza (6 mesi)
        boolean isExpired = selected.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(6));
        String prefix = isExpired ? "ATTENZIONE: Password scaduta (più di 6 mesi)!\n\n" : "";
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dettagli Credenziali");
        alert.setHeaderText(selected.getServiceName());
        alert.setContentText(prefix + "Username: " + selected.getUsername() + "\nPassword: " + decrypted);
        alert.showAndWait();
    }

    @FXML
    private void handleShowPassword() {
        PasswordEntryEntity selected = passwordTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showPasswordDetails(selected);
        }
    }

    @FXML
    private void handleTableClick(javafx.scene.input.MouseEvent event) {
        if (event.getClickCount() == 2) {
            handleShowPassword();
        }
    }

    /**
     * Mostra l'overlay per l'aggiunta di un segreto
     */
    @FXML
    private void handleAddEntry() {
        // Pulisce i campi prima di mostrare
        newServiceField.clear();
        newUsernameField.clear();
        newPasswordField.clear();
        expiryComboBox.setValue(0);
        
        // Rende visibile l'overlay e la card
        overlay.setVisible(true);
        addEntryCard.setVisible(true);
    }

    /**
     * Chiude l'overlay
     */
    @FXML
    private void handleCloseOverlay() {
        overlay.setVisible(false);
        addEntryCard.setVisible(false);
        generatePasswordCard.setVisible(false);
        safeSendOverlayCard.setVisible(false);
        aliasOverlayCard.setVisible(false);
    }

    @FXML
    private void handleNewSafeSend() {
        safeSendTextArea.clear();
        safeSendIdField.clear();
        safeSendResultArea.clear();
        oneTimeCheckBox.setSelected(true);
        overlay.setVisible(true);
        safeSendOverlayCard.setVisible(true);
    }

    @FXML
    private void handleConfirmSafeSend() {
        String content = safeSendTextArea.getText();
        if (content == null || content.isEmpty()) return;
        
        String link = safeSendService.createSafeLink(content, 24, oneTimeCheckBox.isSelected());
        copyToClipboard(link);
        showToast("Link SafeSend copiato negli appunti!");
        safeSendTextArea.clear();
    }

    @FXML
    private void handleAccessSafeSend() {
        String input = safeSendIdField.getText();
        if (input == null || input.isEmpty()) return;

        try {
            // Estrai l'UUID se è un URL completo o usa l'ID così com'è
            String idStr = input.contains("/") ? input.substring(input.lastIndexOf("/") + 1) : input;
            UUID id = UUID.fromString(idStr);
            String decrypted = safeSendService.accessSafeLink(id);
            safeSendResultArea.setText(decrypted);
            showToast("Messaggio decifrato!");
        } catch (Exception e) {
            safeSendResultArea.setText("ERRORE: " + e.getMessage());
            showToast("Impossibile accedere al link.");
        }
    }

    @FXML
    private void handleGenerateAlias() {
        refreshAliasList();
        aliasServiceField.clear();
        overlay.setVisible(true);
        aliasOverlayCard.setVisible(true);
    }

    @FXML
    private void handleConfirmGenerateAlias() {
        String service = aliasServiceField.getText();
        if (service == null || service.trim().isEmpty()) {
            service = "General";
        }
        emailAliasService.generateAlias(service);
        refreshAliasList();
        aliasServiceField.clear();
        showToast("Nuovo alias generato!");
    }

    private void refreshAliasList() {
        List<String> aliases = emailAliasService.getAliasesForCurrentUser().stream()
                .map(a -> a.getServiceName() + ": " + a.getAliasEmail())
                .collect(Collectors.toList());
        aliasListView.setItems(FXCollections.observableArrayList(aliases));
    }

    /**
     * Conferma il salvataggio e invoca il VaultService
     */
    @FXML
    private void handleConfirmSave() {
        String service = newServiceField.getText();
        String user = newUsernameField.getText();
        String pass = newPasswordField.getText();
        Integer expiryHours = expiryComboBox.getValue();

        if (service == null || service.isBlank() || pass == null || pass.isBlank()) {
            // Qui si potrebbe aggiungere un feedback visivo di errore
            return;
        }

        LocalDateTime expiresAt = null;
        if (expiryHours != null && expiryHours > 0) {
            expiresAt = LocalDateTime.now().plusHours(expiryHours);
        }

        // 1. Cifratura e salvataggio (gestito internamente da VaultService)
        vaultService.addEntry(service, user, pass, expiresAt);

        // 2. Chiudi overlay
        handleCloseOverlay();
    }

    @Override
    public void onVaultChanged() {
        javafx.application.Platform.runLater(this::refreshVault);
    }

    @FXML
    private void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
        updateHealthScore();
    }

    private void updateHealthScore() {
        SecurityAuditService.AuditResult result = auditService.runAudit(masterData);
        healthScoreLabel.setText(result.score() + "/100");
        
        if (result.score() >= 80) {
            healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #15803d;");
        } else if (result.score() >= 50) {
            healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #d97706;");
        } else {
            healthScoreLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 800; -fx-text-fill: #dc2626;");
        }

        StringBuilder detail = new StringBuilder();
        if (result.reusedCount() > 0) detail.append("⚠ ").append(result.reusedCount()).append(" password riutilizzate\n");
        if (result.weakCount() > 0) detail.append("⚠ ").append(result.weakCount()).append(" password deboli\n");
        if (result.oldCount() > 0) detail.append("⚠ ").append(result.oldCount()).append(" password vecchie");
        
        if (detail.length() == 0) {
            auditDetailLabel.setText("Il tuo vault è sicuro.");
        } else {
            auditDetailLabel.setText(detail.toString().trim());
        }
    }

    @FXML
    private void handleFullAudit() {
        SecurityAuditService.AuditResult result = auditService.runAudit(masterData);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Report Sicurezza");
        alert.setHeaderText("Analisi Approfondita del Vault");
        
        String content = String.format(
            "Punteggio Salute: %d/100\n\n" +
            "Dettagli:\n" +
            "- Password riutilizzate: %d\n" +
            "- Password deboli: %d\n" +
            "- Password più vecchie di 1 anno: %d",
            result.score(), result.reusedCount(), result.weakCount(), result.oldCount()
        );
        
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }

    private void showToast(String msg) {
        toastLabel.setText(msg);
        toastLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.seconds(2), toastLabel);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> toastLabel.setVisible(false));
        ft.play();
    }

    @FXML private void handleLogout() {
        SessionContext.logout();
        SceneNavigator.switchTo((Stage) userLabel.getScene().getWindow(), "/com/safecore/ui/view/login.fxml", "Login");
    }

    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Backup Vault");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SafeCore Backup (*.safecore)", "*.safecore"));
        fileChooser.setInitialFileName("vault_backup_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".safecore");
        
        File file = fileChooser.showSaveDialog(overlay.getScene().getWindow());
        if (file != null) {
            try {
                vaultService.exportVaultAsEncryptedJson(file);
                showToast("Backup eseguito con successo!");
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Errore Backup");
                alert.setHeaderText("Impossibile eseguire il backup");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML 
    private void handleGeneratePassword() {
        String password = passwordGenerator.generateSafe(16);
        generatedPasswordField.setText(password);
        
        overlay.setVisible(true);
        generatePasswordCard.setVisible(true);
    }

    @FXML
    private void handleConfirmCopy() {
        copyToClipboard(generatedPasswordField.getText());
        handleCloseOverlay();
        showToast("Password copiata!");
    }

    private void applyHoverAnimation(VBox card) {
        // Rimosso stile inline per favorire le classi CSS .card:hover definite in style.css
    }
}