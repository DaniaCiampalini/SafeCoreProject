package com.safecore.business.hints.rules;

import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class MinLengthRule implements PasswordRule {
    @Override
    public Optional<PasswordHint> check(String password) {
        // Aggiunto check null-safe
        if (password == null || password.length() < 8) {
            return Optional.of(new PasswordHint(
                    "Password troppo corta (minimo 8 caratteri)",
                    HintLevel.WARNING
            ));
        }
        return Optional.empty();
    }
}