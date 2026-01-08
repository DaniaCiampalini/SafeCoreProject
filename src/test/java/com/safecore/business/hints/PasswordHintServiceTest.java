package com.safecore.business.hints;

import com.safecore.business.service.PasswordHintService;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHintServiceTest {

    private final PasswordHintService hintService = new PasswordHintService();

    @Test
    void testGetHintsForWeakPassword() {
        // Password corta e senza simboli
        List<PasswordHint> hints = hintService.getHints("123");

        assertFalse(hints.isEmpty(), "Dovrebbe generare suggerimenti per una password debole");
        assertTrue(hints.stream().anyMatch(h -> h.getMessage().contains("lunghezza")),
                "Dovrebbe suggerire la lunghezza minima");
    }

    @Test
    void testNoHintsForStrongPassword() {
        List<PasswordHint> hints = hintService.getHints("Str0ngP@ssw0rd2026!");
        assertTrue(hints.isEmpty(), "Una password forte non dovrebbe avere suggerimenti");
    }
}