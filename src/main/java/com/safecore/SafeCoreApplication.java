package com.safecore;

import com.safecore.ui.AppLauncher;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto di ingresso principale dell'applicazione Spring Boot + JavaFX
**/

@SpringBootApplication
public class SafeCoreApplication {

    public static void main(String[] args) {

        Application.launch(AppLauncher.class, args);
    }
}