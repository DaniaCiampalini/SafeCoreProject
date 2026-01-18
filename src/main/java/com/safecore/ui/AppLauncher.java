package com.safecore.ui;

import com.safecore.SafeCoreApplication;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Launcher dell'applicazione JavaFX integrata con Spring Boot.
 * Qui avviene il boot di Spring e il setup iniziale della UI.
 */

public class AppLauncher extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(SafeCoreApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (springContext != null) {
                springContext.getBean(GlobalExceptionHandler.class).handle(e);
            } else {
                e.printStackTrace(); //TODO: printStackTrace() not recommended. Consider using a proper logging framework SLF4J
            }
        });

        try {
            SceneNavigator.setContext(springContext);

            SceneNavigator.switchTo(primaryStage, "/com/safecore/ui/view/login.fxml", "SafeCore – Secure Vault");

            primaryStage.centerOnScreen();

            System.out.println("=== SafeCore: Interfaccia UI Avviata ===");

        } catch (Exception e) {
            System.err.println("ERRORE FATALE all'avvio della UI:");
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}