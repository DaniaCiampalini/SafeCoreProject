package com.safecore.ui;

import com.safecore.SafeCoreApplication;
import com.safecore.business.service.UserService;
import com.safecore.business.service.PasswordService;
import com.safecore.ui.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class AppLauncher extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Avvio di Spring Boot
        springContext = new SpringApplicationBuilder(SafeCoreApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Colleghiamo il Navigator a Spring (Fondamentale per i cambi scena!)
            SceneNavigator.setContext(springContext);

            // Eseguiamo la Demo (opzionale)
            eseguiDemoSilenziosa();

            // 2. Usiamo il Navigator per la prima scena (più pulito)
            // Oppure carichiamo manualmente la prima volta così:
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/safecore/ui/view/login.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Scene scene = new Scene(loader.load(), 400, 500);

            primaryStage.setTitle("SafeCore – Secure Vault");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Opzionale: rende la UI più professionale
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        // Spegnimento pulito di Spring (chiude connessioni DB H2)
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    private void eseguiDemoSilenziosa() {
        try {
            UserService userService = springContext.getBean(UserService.class);
            System.out.println("=== SafeCore: Spring Context Ready ===");
            // Esempio: System.out.println("Utenti registrati: " + userService.findAll().size());
        } catch (Exception e) {
            System.err.println("Errore durante la demo: " + e.getMessage());
        }
    }
}