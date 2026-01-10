package com.safecore;

import com.safecore.ui.AppLauncher;
import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SafeCoreApplication {

    public static void main(String[] args) {
        // Qui succede una cosa un po' particolare: invece di far partire Spring come al solito,
        // lanciamo prima JavaFX (AppLauncher). È JavaFX che poi "si tira dietro" Spring.
        // Questo serve perché JavaFX ha bisogno del suo thread specifico per la UI.
        Application.launch(AppLauncher.class, args);
    }
}