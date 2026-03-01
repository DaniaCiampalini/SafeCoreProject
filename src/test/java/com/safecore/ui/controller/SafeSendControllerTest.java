package com.safecore.ui.controller;

import com.safecore.business.service.UserService;
import com.safecore.ui.TestFXBaseTest;
import com.safecore.ui.session.SessionContext;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test TestFX per SafeSendController.
 * Verifica la funzionalità di condivisione sicura tramite link.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SafeSendControllerTest extends TestFXBaseTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    private void loadSafeSendView() {
        runOnFxThread(() -> {
            try {
                userService.register("safesend@test.com", "SafeSendPass123!");
                SessionContext.login("safesend@test.com");

                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/safecore/ui/view/safesend-view.fxml")
                );
                loader.setControllerFactory(applicationContext::getBean);
                Parent root = loader.load();

                stage.setScene(new Scene(root));
                stage.setTitle("SafeSend");
                stage.show();
            } catch (Exception e) {
                throw new RuntimeException("Failed to load safesend view", e);
            }
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testSafeSendViewLoadsSuccessfully() {
        loadSafeSendView();

        TextArea safeSendTextArea = lookup("#safeSendTextArea").query();
        assertNotNull(safeSendTextArea);
    }

    @Test
    void testAllUIElementsArePresent() {
        loadSafeSendView();

        assertNotNull(lookup("#safeSendTextArea").query());
        assertNotNull(lookup("#safeSendIdField").query());
        assertNotNull(lookup("#safeSendResultArea").query());
        assertNotNull(lookup("#safeSendExpiryCombo").query());
    }

    @Test
    void testExpiryComboBoxHasOptions() {
        loadSafeSendView();

        ComboBox<Integer> expiryCombo = lookup("#safeSendExpiryCombo").query();
        assertNotNull(expiryCombo);
        assertFalse(expiryCombo.getItems().isEmpty(),
                "Combo box dovrebbe avere opzioni di scadenza");
        assertTrue(expiryCombo.getItems().contains(24),
                "Dovrebbe contenere l'opzione 24 ore");
    }

    @Test
    void testExpiryComboBoxDefaultValue() {
        loadSafeSendView();

        ComboBox<Integer> expiryCombo = lookup("#safeSendExpiryCombo").query();
        assertEquals(24, expiryCombo.getValue(),
                "Il valore predefinito dovrebbe essere 24 ore");
    }

    @Test
    void testTextAreaAcceptsInput() {
        loadSafeSendView();

        TextArea safeSendTextArea = lookup("#safeSendTextArea").query();
        String testContent = "Test secret message";

        interact(() -> safeSendTextArea.setText(testContent));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(testContent, safeSendTextArea.getText());
    }

    @Test
    void testResultAreaIsReadOnly() {
        loadSafeSendView();

        TextArea safeSendResultArea = lookup("#safeSendResultArea").query();
        assertFalse(safeSendResultArea.isEditable(),
                "Result area dovrebbe essere read-only");
    }
}

