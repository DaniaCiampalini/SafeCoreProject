package com.safecore.business.service;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Servizio legacy di auto-compilazione desktop basato su Robot.
 * <p>
 * Non viene più utilizzato nell'applicazione principale per evitare
 * integrazioni a livello di OS e comportamenti non deterministici.
 */
@Deprecated
@Service
public class AutoFillService {

    private final Robot robot;

    public AutoFillService() {
        // Se siamo in un ambiente senza interfaccia grafica (es. durante i test su un server)
        // il Robot non può essere inizializzato.
        if (GraphicsEnvironment.isHeadless()) {
            this.robot = null;
        } else {
            try {
                // Il Robot è una classe di Java che "prende il controllo" di mouse e tastiera.
                this.robot = new Robot();
                // Mettiamo un piccolo delay tra un tasto e l'altro per non sembrare troppo "macchine"
                // e per dare tempo al sistema di elaborare gli input.
                this.robot.setAutoDelay(20);
            } catch (AWTException e) {
                throw new RuntimeException("Impossibile inizializzare il servizio di AutoFill", e);
            }
        }
    }

    public void typeText(String text) {
        if (robot == null) return;
        // Aspettiamo mezzo secondo per sicurezza.
        robot.delay(500);

        for (char c : text.toCharArray()) {
            typeChar(c);
        }
    }

    /**
     * Simula la digitazione di username e password.
     * È la magia che permette di fare login automatico nei siti!
     */
    public void typeCredentials(String username, String password) {
        // Pausa di 2 secondi: dà il tempo all'utente di cliccare sul campo di input del browser.
        robot.delay(2000);

        // Scrive l'username
        typeText(username);

        // Preme TAB per spostarsi sul campo password (funziona nel 99% dei siti)
        robot.keyPress(KeyEvent.VK_TAB);
        robot.keyRelease(KeyEvent.VK_TAB);

        robot.delay(100);

        // Scrive la password
        typeText(password);

        // Preme INVIO per confermare il login
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
