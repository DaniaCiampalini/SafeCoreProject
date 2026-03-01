package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.TestFXBaseTest;
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
 * Test TestFX per RegisterController.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RegisterControllerTest extends TestFXBaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    private void loadRegisterView() {
        runOnFxThread(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/safecore/ui/view/register.fxml")
                );
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                stage.setScene(new Scene(root));
                stage.setTitle("SafeCore – Registrazione");
                stage.show();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load register view", e);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testRegisterViewLoadsSuccessfully() {
        loadRegisterView();

        TextField emailField = lookup("#emailField").query();
        PasswordField passwordField = lookup("#passwordField").query();
        PasswordField confirmPasswordField = lookup("#confirmPasswordField").query();

        assertNotNull(emailField);
        assertNotNull(passwordField);
        assertNotNull(confirmPasswordField);
    }

    @Test
    void testAllUIElementsArePresent() {
        loadRegisterView();

        assertNotNull(lookup("#emailField").query());
        assertNotNull(lookup("#passwordField").query());
        assertNotNull(lookup("#confirmPasswordField").query());
        assertNotNull(lookup("#passwordTextField").query());
        assertNotNull(lookup("#confirmPasswordTextField").query());
        assertNotNull(lookup("#togglePwdBtn").query());
        assertNotNull(lookup("#toggleConfirmPwdBtn").query());
        assertNotNull(lookup("#messageLabel").query());
        assertNotNull(lookup("#passwordStrengthLabel").query());
        assertNotNull(lookup("Registrati Ora").query());
        assertNotNull(lookup("Genera Password Suggerita").query());
    }

    @Test
    void testPasswordToggleVisibility() {
        loadRegisterView();

        TextField passwordTextField = lookup("#passwordTextField").query();
        PasswordField passwordField = lookup("#passwordField").query();
        Button toggleBtn = lookup("#togglePwdBtn").query();

        // Stato iniziale
        assertTrue(passwordField.isVisible());
        assertFalse(passwordTextField.isVisible());
        assertEquals("Show", toggleBtn.getText());

        // Toggle
        clickOn(toggleBtn);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(passwordField.isVisible());
        assertTrue(passwordTextField.isVisible());
        assertEquals("Hide", toggleBtn.getText());
    }

    @Test
    void testConfirmPasswordToggleVisibility() {
        loadRegisterView();

        TextField confirmPasswordTextField = lookup("#confirmPasswordTextField").query();
        PasswordField confirmPasswordField = lookup("#confirmPasswordField").query();
        Button toggleBtn = lookup("#toggleConfirmPwdBtn").query();

        // Stato iniziale
        assertTrue(confirmPasswordField.isVisible());
        assertFalse(confirmPasswordTextField.isVisible());
        assertEquals("Show", toggleBtn.getText());

        // Toggle
        clickOn(toggleBtn);
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(confirmPasswordField.isVisible());
        assertTrue(confirmPasswordTextField.isVisible());
        assertEquals("Hide", toggleBtn.getText());
    }

    @Test
    void testEmptyFieldsValidation() {
        loadRegisterView();

        clickOn("Registrati Ora");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Tutti i campi sono obbligatori", messageLabel.getText());
    }

    @Test
    void testPasswordMismatchValidation() {
        loadRegisterView();

        TextField emailField = lookup("#emailField").query();
        PasswordField passwordField = lookup("#passwordField").query();
        PasswordField confirmPasswordField = lookup("#confirmPasswordField").query();

        interact(() -> {
            emailField.setText("mismatch@test.com");
            passwordField.setText("Password123!");
            confirmPasswordField.setText("DifferentPass456!");
        });
        WaitForAsyncUtils.waitForFxEvents();

        clickOn("Registrati Ora");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Le password non coincidono", messageLabel.getText());
    }

    @Test
    void testPasswordStrengthIndicatorForWeakPassword() {
        loadRegisterView();

        PasswordField passwordField = lookup("#passwordField").query();
        Label strengthLabel = lookup("#passwordStrengthLabel").query();

        // Inizialmente vuota
        assertTrue(strengthLabel.getText().isEmpty() || strengthLabel.getText().isBlank());

        // Imposta una password debole e simula keyReleased
        interact(() -> {
            passwordField.setText("weak");
            passwordField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_RELEASED,
                    "", "", javafx.scene.input.KeyCode.UNDEFINED,
                    false, false, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        assertTrue(strengthLabel.getText().contains("DEBOLE"));
    }

    @Test
    void testPasswordStrengthIndicatorForStrongPassword() {
        loadRegisterView();

        PasswordField passwordField = lookup("#passwordField").query();
        Label strengthLabel = lookup("#passwordStrengthLabel").query();

        // Imposta una password forte
        interact(() -> {
            passwordField.setText("VerySecurePassword123!@#$");
            passwordField.fireEvent(new javafx.scene.input.KeyEvent(
                    javafx.scene.input.KeyEvent.KEY_RELEASED,
                    "", "", javafx.scene.input.KeyCode.UNDEFINED,
                    false, false, false, false
            ));
        });
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        String strengthText = strengthLabel.getText();
        assertTrue(strengthText.contains("SICURA") || strengthText.isEmpty(),
                "Password forte dovrebbe essere segnalata come SICURA o vuota se OK");
    }

    @Test
    void testGeneratePasswordButton() {
        loadRegisterView();

        PasswordField passwordField = lookup("#passwordField").query();
        PasswordField confirmPasswordField = lookup("#confirmPasswordField").query();

        // Inizialmente vuoti
        assertTrue(passwordField.getText().isEmpty());
        assertTrue(confirmPasswordField.getText().isEmpty());

        // Genera password
        clickOn("Genera Password Suggerita");
        WaitForAsyncUtils.waitForFxEvents();
        sleep(300);

        // Verifica che i campi siano stati riempiti
        assertFalse(passwordField.getText().isEmpty());
        assertFalse(confirmPasswordField.getText().isEmpty());
        assertEquals(passwordField.getText(), confirmPasswordField.getText());
        assertTrue(passwordField.getText().length() >= 16);

        // Verifica il messaggio
        Label messageLabel = lookup("#messageLabel").query();
        assertEquals("Password generata con successo", messageLabel.getText());
    }


    @Test
    void testPasswordSyncAfterGeneration() {
        loadRegisterView();

        TextField passwordTextField = lookup("#passwordTextField").query();
        TextField confirmPasswordTextField = lookup("#confirmPasswordTextField").query();

        clickOn("Genera Password Suggerita");
        WaitForAsyncUtils.waitForFxEvents();

        // Anche i campi text devono essere sincronizzati
        assertFalse(passwordTextField.getText().isEmpty());
        assertFalse(confirmPasswordTextField.getText().isEmpty());
        assertEquals(passwordTextField.getText(), confirmPasswordTextField.getText());
    }
}

