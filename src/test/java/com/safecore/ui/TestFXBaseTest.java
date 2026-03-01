package com.safecore.ui;

import com.safecore.SafeCoreApplication;
import com.safecore.ui.session.SessionContext;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Classe base per i test TestFX.
 * Configura l'ambiente Spring Boot e JavaFX per i test UI.
 */
@SpringBootTest(classes = SafeCoreApplication.class)
@ActiveProfiles("test")
public abstract class TestFXBaseTest extends ApplicationTest {

    protected Stage stage;
    private static boolean javaFxInitialized = false;

    @BeforeAll
    public static void setupJavaFX() throws Exception {
        if (!javaFxInitialized) {
            // Inizializza il toolkit JavaFX una sola volta
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            boolean initialized = latch.await(5, TimeUnit.SECONDS);
            if (!initialized) {
                throw new RuntimeException("JavaFX initialization timeout");
            }
            javaFxInitialized = true;
        }
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.show();
    }

    @BeforeEach
    public void setUpTest() {
        // Cleanup della sessione prima di ogni test
        SessionContext.logout();
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    public void tearDownTest() {
        // Cleanup dopo ogni test
        WaitForAsyncUtils.waitForFxEvents();
        SessionContext.logout();
    }

    /**
     * Helper per eseguire azioni sulla UI thread e attendere il completamento
     */
    protected void runOnFxThread(Runnable action) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            boolean completed = latch.await(5, TimeUnit.SECONDS);
            if (!completed) {
                throw new RuntimeException("Timeout waiting for FX thread");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for FX thread", e);
        }
    }

    /**
     * Helper per attendere che la UI si aggiorni
     */
    protected void waitForFxEvents() {
        WaitForAsyncUtils.waitForFxEvents();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

