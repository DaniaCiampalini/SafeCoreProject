package com.safecore;

import com.safecore.ui.AppLauncher;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SafeCoreApplication {

    public static void main(String[] args) {
        // Avviamo JavaFX. Sarà JavaFX a far partire Spring, non viceversa.
        Application.launch(AppLauncher.class, args);
    }
}