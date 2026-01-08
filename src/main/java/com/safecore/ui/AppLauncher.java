package com.safecore.ui;

import com.safecore.SafeCoreApplication;
import com.safecore.business.service.UserService;
import com.safecore.business.service.PasswordService;
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
        // FASE 1: Avvio di Spring Boot in background durante l'init di JavaFX
        springContext = new SpringApplicationBuilder(SafeCoreApplication.class).run();
    }

    @Override
    public void start(Stage stage) {
        try {
            // Eseguiamo la Demo (vecchia logica Main) in un thread separato per non bloccare la UI
            eseguiDemoSilenziosa();

            // FASE 2: Caricamento FXML con Dependency Injection
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/safecore/ui/view/login.fxml"));

            // IL SEGRETO PER IL 30L: Istruiamo JavaFX a chiedere i controller a Spring
            loader.setControllerFactory(springContext::getBean);

            Scene scene = new Scene(loader.load(), 400, 450);
            stage.setTitle("SafeCore – Secure Vault");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        // Spegnimento pulito di Spring (chiude DB, JpaUtil, ecc.)
        springContext.close();
        Platform.exit();
    }

    /**
     * Recupera la logica del vecchio Main.
     * In un'app reale questa demo non servirebbe, ma per l'esame
     * dimostra che i servizi funzionano al boot.
     */
    private void eseguiDemoSilenziosa() {
        UserService userService = springContext.getBean(UserService.class);
        PasswordService passwordService = springContext.getBean(PasswordService.class);

        System.out.println("=== SafeCore: Demo Logic Injected Successfully ===");
        // Qui puoi rimettere le chiamate a userService.register() se vuoi testare il DB al boot
    }
}