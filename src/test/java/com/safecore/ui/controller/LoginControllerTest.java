package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.TestFXBaseTest;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TestFX per LoginController.
 * Focus su test stabili e funzionanti.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LoginControllerTest extends TestFXBaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    private void loadLoginView() {
        runOnFxThread(() -> {
            try {
                userService.register("testuser@example.com", "TestPass123!");

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/safecore/ui/view/login.fxml")
                );
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                stage.setScene(new Scene(root));
                stage.setTitle("SafeCore – Login");
                stage.show();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load login view", e);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testLoginViewLoadsSuccessfully() {
        loadLoginView();

        TextField emailField = lookup("#emailField").query();
        PasswordField passwordField = lookup("#passwordField").query();

        assertNotNull(emailField);
        assertNotNull(passwordField);
    }

    @Test
    void testAllUIElementsArePresent() {
        loadLoginView();

        assertNotNull(lookup("#emailField").query());
        assertNotNull(lookup("#passwordField").query());
        assertNotNull(lookup("#passwordTextField").query());
        assertNotNull(lookup("#togglePasswordBtn").query());
        assertNotNull(lookup("#messageLabel").query());
        assertNotNull(lookup("Accedi al Vault").query());
    }

    @Test
    void testPasswordFieldsVisibilityToggle() {
        loadLoginView();

        TextField passwordTextField = lookup("#passwordTextField").query();
        PasswordField passwordField = lookup("#passwordField").query();
        Button toggleBtn = lookup("#togglePasswordBtn").query();

        // Stato iniziale
        assertTrue(passwordField.isVisible());
        assertFalse(passwordTextField.isVisible());
        assertEquals("Show", toggleBtn.getText());

        // Primo toggle
        clickOn(toggleBtn);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(passwordField.isVisible());
        assertTrue(passwordTextField.isVisible());
        assertEquals("Hide", toggleBtn.getText());

        // Secondo toggle
        clickOn(toggleBtn);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(passwordField.isVisible());
        assertFalse(passwordTextField.isVisible());
        assertEquals("Show", toggleBtn.getText());
    }

    @Test
    void testPasswordSyncBetweenFields() {
        loadLoginView();

        TextField passwordTextField = lookup("#passwordTextField").query();
        PasswordField passwordField = lookup("#passwordField").query();
        Button toggleBtn = lookup("#togglePasswordBtn").query();

        String testPassword = "SyncTest123";

        // Imposta password nel campo nascosto
        interact(() -> passwordField.setText(testPassword));
        WaitForAsyncUtils.waitForFxEvents();

        // Toggle per mostrare
        clickOn(toggleBtn);
        WaitForAsyncUtils.waitForFxEvents();
        sleep(200);

        // Verifica la sincronizzazione
        assertEquals(testPassword, passwordTextField.getText());
    }

    @Test
    void testEmptyFieldsValidation() {
        loadLoginView();

        clickOn("Accedi al Vault");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Email e password sono obbligatorie", messageLabel.getText());
    }

    @Test
    void testEmailOnlyValidation() {
        loadLoginView();

        TextField emailField = lookup("#emailField").query();
        interact(() -> emailField.setText("only@email.com"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Accedi al Vault");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Email e password sono obbligatorie", messageLabel.getText());
    }

    @Test
    void testPasswordOnlyValidation() {
        loadLoginView();

        PasswordField passwordField = lookup("#passwordField").query();
        interact(() -> passwordField.setText("OnlyPassword123"));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Accedi al Vault");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Email e password sono obbligatorie", messageLabel.getText());
    }

    @Test
    void testInvalidCredentialsShowsError() {
        loadLoginView();

        TextField emailField = lookup("#emailField").query();
        PasswordField passwordField = lookup("#passwordField").query();

        interact(() -> {
            emailField.setText("wrong@example.com");
            passwordField.setText("WrongPassword");
        });
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Accedi al Vault");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Credenziali non valide", messageLabel.getText());
    }
}

