package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.TestFXBaseTest;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TestFX per SettingsController.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SettingsControllerTest extends TestFXBaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    private static final String CONFIRMATION_PHRASE = "Elimina il mio account SafeCore";

    private void loadSettingsView() {
        runOnFxThread(() -> {
            try {
                userService.register("settings@example.com", "SettingsPass123!");
                SessionContext.login("settings@example.com");

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/safecore/ui/view/settings-view.fxml")
                );
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                stage.setScene(new Scene(root));
                stage.setTitle("Settings");
                stage.show();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load settings view", e);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testSettingsViewLoadsSuccessfully() {
        loadSettingsView();

        Label emailLabel = lookup("#emailLabel").query();
        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        assertNotNull(emailLabel);
        assertNotNull(confirmationField);
        assertNotNull(deleteButton);
    }

    @Test
    void testAllUIElementsArePresent() {
        loadSettingsView();

        assertNotNull(lookup("#emailLabel").query());
        assertNotNull(lookup("#confirmationField").query());
        assertNotNull(lookup("#deleteButton").query());
        assertNotNull(lookup("Chiudi").query());
    }

    @Test
    void testEmailLabelShowsCurrentUser() {
        loadSettingsView();

        Label emailLabel = lookup("#emailLabel").query();
        assertEquals("settings@example.com", emailLabel.getText());
    }

    @Test
    void testDeleteButtonInitiallyDisabled() {
        loadSettingsView();

        Button deleteButton = lookup("#deleteButton").query();
        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testDeleteButtonEnabledWithCorrectPhrase() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        interact(() -> confirmationField.setText(CONFIRMATION_PHRASE));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        assertFalse(deleteButton.isDisabled());
    }

    @Test
    void testDeleteButtonDisabledWithWrongPhrase() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        interact(() -> confirmationField.setText("Frase sbagliata"));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testDeleteButtonStyleChangesWhenEnabled() {
        loadSettingsView();

        Button deleteButton = lookup("#deleteButton").query();
        TextField confirmationField = lookup("#confirmationField").query();

        // Stile iniziale (grigio/disabilitato)
        String initialStyle = deleteButton.getStyle();
        assertTrue(initialStyle.contains("#9ca3af"));

        // Inserisce la frase corretta
        interact(() -> confirmationField.setText(CONFIRMATION_PHRASE));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(200);

        // Stile cambiato (rosso/abilitato)
        String newStyle = deleteButton.getStyle();
        assertTrue(newStyle.contains("#dc2626"));
    }

    @Test
    void testPartialPhraseKeepsButtonDisabled() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        interact(() -> confirmationField.setText("Elimina il mio"));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testCaseSensitivePhrase() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        // Lowercase (sbagliato)
        interact(() -> confirmationField.setText("elimina il mio account safecore"));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testExtraSpacesInPhrase() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        interact(() -> confirmationField.setText("Elimina  il mio account SafeCore"));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testClearingFieldDisablesButton() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        // Inserisce la frase corretta
        interact(() -> confirmationField.setText(CONFIRMATION_PHRASE));
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(deleteButton.isDisabled());

        // Cancella
        interact(() -> confirmationField.setText(""));
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testReEnablingAfterCorrectPhrase() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        Button deleteButton = lookup("#deleteButton").query();

        // Frase sbagliata
        interact(() -> confirmationField.setText("Wrong phrase"));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(deleteButton.isDisabled());

        // Frase corretta
        interact(() -> confirmationField.setText(CONFIRMATION_PHRASE));
        WaitForAsyncUtils.waitForFxEvents();
        sleep(200);
        assertFalse(deleteButton.isDisabled());

        // Di nuovo sbagliata
        interact(() -> confirmationField.setText("Wrong again"));
        WaitForAsyncUtils.waitForFxEvents();
        assertTrue(deleteButton.isDisabled());
    }

    @Test
    void testConfirmationFieldPromptText() {
        loadSettingsView();

        TextField confirmationField = lookup("#confirmationField").query();
        assertEquals("Digita qui la frase di conferma...", confirmationField.getPromptText());
    }

    @Test
    void testDeleteButtonText() {
        loadSettingsView();

        Button deleteButton = lookup("#deleteButton").query();
        assertEquals("Conferma Eliminazione", deleteButton.getText());
    }

    @Test
    void testWarningLabelsAreVisible() {
        loadSettingsView();

        // Verifica che ci siano label di avviso
        Label warningLabel = lookup("⚠ ATTENZIONE: Questa azione è IRREVERSIBILE!").query();
        assertNotNull(warningLabel);
        assertTrue(warningLabel.isVisible());
    }

    @Test
    void testCloseButton() {
        loadSettingsView();

        Button closeButton = lookup("Chiudi").query();
        assertNotNull(closeButton);
        assertFalse(closeButton.isDisabled());
    }
}

