package com.safecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SafeCoreApplication {

    public static void main(String[] args) {
        // SpringApplication.run fa tutto quello che facevi manualmente:
        // Inizializza il DB, crea i servizi, configura lo spegnimento.
        SpringApplication.run(SafeCoreApplication.class, args);
    }
}