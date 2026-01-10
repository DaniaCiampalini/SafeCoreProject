package com.safecore.business.hints.rules;

import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Regola: la password deve essere un mix di caratteri diversi.
 * Usare solo lettere minuscole è come chiudere la porta di casa lasciando la chiave nella serratura.
 */
@Component
public class ComplexityRule implements PasswordRule {
    @Override
    public Optional<PasswordHint> check(String password) {
        if (password == null) return Optional.empty();

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        if (!(hasUpper && hasLower && hasDigit)) {
            return Optional.of(new PasswordHint(
                    "Mixala un po': aggiungi maiuscole e numeri!",
                    HintLevel.INFO
            ));
        }
        return Optional.empty();
    }
}