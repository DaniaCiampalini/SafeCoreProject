package com.safecore.business.hints;

import com.safecore.business.hints.rules.ComplexityRule;
import com.safecore.business.hints.rules.MinLengthRule;
import com.safecore.business.service.PasswordHintService;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHintServiceTest {

    // Creiamo il servizio passando manualmente le regole che Spring inietterebbe nell'app reale
    private final PasswordHintService hintService = new PasswordHintService(
            Arrays.asList(new MinLengthRule(), new ComplexityRule())
    );

    @Test
    void testGetHintsForWeakPassword() {
        // Password corta (3 caratteri)
        List<PasswordHint> hints = hintService.getHints("123");

        boolean hasLengthHint = hints.stream()
                .map(PasswordHint::getMessage)
                .anyMatch(msg -> msg.toLowerCase().contains("minimo 8 caratteri")
                        || msg.toLowerCase().contains("corta"));

        assertTrue(hasLengthHint, "Dovrebbe suggerire la lunghezza minima");
    }

    @Test
    void testGetHintsForSimplePassword() {
        // Password lunga ma senza maiuscole
        List<PasswordHint> hints = hintService.getHints("password123");

        boolean hasComplexityHint = hints.stream()
                .map(PasswordHint::getMessage)
                .anyMatch(msg -> msg.toLowerCase().contains("maiuscole"));

        assertTrue(hasComplexityHint, "Dovrebbe suggerire di aumentare la complessità");
    }
}