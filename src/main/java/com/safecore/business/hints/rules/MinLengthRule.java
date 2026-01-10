package com.safecore.business.hints.rules;

import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;
import org.springframework.stereotype.Component;
import java.util.Optional;

/**
 * Regola: la password deve essere lunga almeno 8 caratteri.
 * Sotto questa soglia, la crittografia serve a poco perché è troppo facile da indovinare.
 */
@Component
public class MinLengthRule implements PasswordRule {
    @Override
    public Optional<PasswordHint> check(String password) {
        if (password == null || password.length() < 8) {
            return Optional.of(new PasswordHint(
                    "Mancano caratteri: ne servono almeno 8.",
                    HintLevel.WARNING
            ));
        }
        return Optional.empty();
    }
}