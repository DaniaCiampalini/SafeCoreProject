package com.safecore.business.hints.rules;

import com.safecore.business.hints.HintLevel;
import com.safecore.business.hints.PasswordHint;

import java.util.Optional;

public class MinLengthRule implements PasswordRule {

    @Override
    public Optional<PasswordHint> evaluate(String password) {

        if (password.length() < 8) {
            return Optional.of(
                    new PasswordHint(
                            "Password troppo corta (minimo 8 caratteri)",
                            HintLevel.WARNING
                    )
            );
        }

        return Optional.empty();
    }
}
