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
        // Qui facciamo il boot di Spring. 
        // Importante: mettiamo headless(false) perché altrimenti il Robot di Java
        // (che usiamo per l'autofill) si arrabbia e non funziona.
        springContext = new SpringApplicationBuilder(SafeCoreApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Questo è il "paracadute" per le eccezioni che scappano via nel thread della UI.
        // Invece di far crashare tutto male, passiamo l'errore al GlobalExceptionHandler
        // che mostrerà un bel popup all'utente.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (springContext != null) {
                springContext.getBean(GlobalExceptionHandler.class).handle(e);
            } else {
                e.printStackTrace();
            }
        });

        try {
            // Passiamo il contesto di Spring al navigatore, così quando carica i controller
            // può iniettare tutte le dipendenze (i Service, ecc.) in automatico.
            SceneNavigator.setContext(springContext);

            // Carichiamo la prima schermata: il login.
            SceneNavigator.switchTo(primaryStage, "/com/safecore/ui/view/login.fxml", "SafeCore – Secure Vault");

            // Centriamo la finestra, che fa sempre la sua figura.
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
        // Quando chiudiamo l'app, ricordiamoci di spegnere anche Spring
        // altrimenti restano i processi appesi.
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }
}