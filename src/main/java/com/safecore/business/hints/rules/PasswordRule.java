package com.safecore.business.hints.rules;

import com.safecore.business.hints.PasswordHint;
import java.util.Optional;

public interface PasswordRule {
    Optional<PasswordHint> check(String password);
}