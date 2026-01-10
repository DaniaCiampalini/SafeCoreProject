package com.safecore.ui.controller;

import com.safecore.business.service.VaultService;
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
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.stereotype.Component;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import org.springframework.context.ApplicationContext;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class DashboardController {

    @FXML private Label userLabel, securityStatusLabel, toastLabel;
    @FXML private TextField searchField;
    @FXML private TableView<PasswordEntryEntity> passwordTable;
    @FXML private TableColumn<PasswordEntryEntity, String> serviceColumn;
    @FXML private TableColumn<PasswordEntryEntity, String> usernameColumn;
    @FXML private TableColumn<PasswordEntryEntity, Void> actionsColumn;
    @FXML private VBox passwordCard, vaultCard, backupCard;

    // Nuovi elementi per l'Overlay
    @FXML private Region overlay;
    @FXML private VBox addEntryCard;
    @FXML private TextField newServiceField;
    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;

    private final PasswordGenerator passwordGenerator;
    private final VaultService vaultService;
    private final ApplicationContext applicationContext;
    private ObservableList<PasswordEntryEntity> masterData = FXCollections.observableArrayList();

    public DashboardController(PasswordGenerator passwordGenerator, VaultService vaultService, ApplicationContext applicationContext) {
        this.passwordGenerator = passwordGenerator;
        this.vaultService = vaultService;
        this.applicationContext = applicationContext;
    }

    @FXML
    private void initialize() {
        userLabel.setText(SessionContext.getCurrentUserEmail());

        // Configurazione colonne
        serviceColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        setupActionsColumn();
        setupSearchFilter();
        refreshVault();

        applyHoverAnimation(passwordCard);
        applyHoverAnimation(vaultCard);
        applyHoverAnimation(backupCard);
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
            private final Button copyBtn = new Button("📋");
            private final Button deleteBtn = new Button("🗑");
            private final HBox container = new HBox(10, copyBtn, deleteBtn);

            {
                copyBtn.setStyle("-fx-cursor: hand; -fx-background-color: #f3f4f6;");
                deleteBtn.setStyle("-fx-cursor: hand; -fx-background-color: #fee2e2; -fx-text-fill: #ef4444;");

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
    private void handleShowPassword() {
        PasswordEntryEntity selected = passwordTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String decrypted = vaultService.decryptPassword(selected.getEncryptedPassword());

            // Logica Scadenza (6 mesi)
            boolean isExpired = selected.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(6));
            String prefix = isExpired ? "⚠️ ATTENZIONE: Password scaduta (più di 6 mesi)!\n\n" : "";

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Dettagli Credenziali");
            alert.setHeaderText(selected.getServiceName());
            alert.setContentText(prefix + "Username: " + selected.getUsername() + "\nPassword: " + decrypted);
            alert.showAndWait();
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
    }

    /**
     * Conferma il salvataggio e invoca il VaultService
     */
    @FXML
    private void handleConfirmSave() {
        String service = newServiceField.getText();
        String user = newUsernameField.getText();
        String pass = newPasswordField.getText();

        if (service == null || service.isBlank() || pass == null || pass.isBlank()) {
            // Qui si potrebbe aggiungere un feedback visivo di errore
            return;
        }

        // 1. Cifratura e salvataggio (gestito internamente da VaultService)
        vaultService.addEntry(service, user, pass);

        // 2. Chiudi overlay
        handleCloseOverlay();

        // 3. Refresh istantaneo della tabella
        refreshVault();
    }

    @FXML
    private void refreshVault() {
        masterData.setAll(vaultService.getEntriesForCurrentUser());
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
        // Implementazione del backup (puoi chiamare il tuo BackupService qui)
        System.out.println("Esecuzione backup in corso...");
    }

    @FXML private void handleGeneratePassword() {
        copyToClipboard(passwordGenerator.generateSafe(16));
        showToast("Password sicura copiata!");
    }

    private void applyHoverAnimation(VBox card) {
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-scale-x: 1.02; -fx-scale-y: 1.02; -fx-border-color: #2563eb; -fx-background-radius: 12; -fx-border-radius: 12;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #e5e7eb;"));
    }
}