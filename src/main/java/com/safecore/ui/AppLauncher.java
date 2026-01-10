package com.safecore.ui;

import com.safecore.SafeCoreApplication;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class AppLauncher extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Avviamo Spring Boot in modalità NON-headless per permettere l'uso di java.awt.Robot
        springContext = new SpringApplicationBuilder(SafeCoreApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Inizializziamo il navigatore con il contesto Spring
            SceneNavigator.setContext(springContext);

            // 2. Usiamo il navigatore per caricare la prima scena
            // Nota: il path deve essere ESATTAMENTE quello dove si trova il file
            SceneNavigator.switchTo(primaryStage, "/com/safecore/ui/view/login.fxml", "SafeCore – Secure Vault");

            // Assicuriamoci che la finestra sia centrata e visibile
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