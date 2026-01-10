package com.safecore.business.service;

import org.springframework.stereotype.Service;
import java.awt.*;
import java.awt.event.KeyEvent;

@Service
public class AutoFillService {

    private final Robot robot;

    public AutoFillService() {
        try {
            this.robot = new Robot();
            this.robot.setAutoDelay(20);
        } catch (AWTException e) {
            throw new RuntimeException("Impossibile inizializzare il servizio di AutoFill", e);
        }
    }

    public void typeText(String text) {
        // Breve pausa per permettere all'utente di cambiare finestra se necessario
        robot.delay(500);
        
        for (char c : text.toCharArray()) {
            typeChar(c);
        }
    }

    public void typeCredentials(String username, String password) {
        // Pausa iniziale di 2 secondi per dare tempo all'utente di cliccare sul campo del browser
        robot.delay(2000);
        
        typeText(username);
        
        // Premi TAB per passare al campo password
        robot.keyPress(KeyEvent.VK_TAB);
        robot.keyRelease(KeyEvent.VK_TAB);
        
        robot.delay(100);
        
        typeText(password);
        
        // Opzionale: Premi INVIO per loggare
        robot.keyPress(KeyEvent.VK_ENTER);
        robot.keyRelease(KeyEvent.VK_ENTER);
    }

    private void typeChar(char c) {
        boolean upperCase = Character.isUpperCase(c);
        int keyCode = KeyEvent.getExtendedKeyCodeForChar(c);

        if (keyCode == KeyEvent.VK_UNDEFINED) {
            // Gestione simboli speciali se necessario
            return;
        }

        if (upperCase) {
            robot.keyPress(KeyEvent.VK_SHIFT);
        }

        try {
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            // Salta se il tasto non è supportato dal Robot sulla piattaforma corrente
        }

        if (upperCase) {
            robot.keyRelease(KeyEvent.VK_SHIFT);
        }
    }
}
