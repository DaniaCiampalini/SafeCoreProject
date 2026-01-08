package com.safecore.business.service;

import com.safecore.business.hints.PasswordHint;
import com.safecore.business.hints.rules.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PasswordHintService {

    // Spring inietta automaticamente tutti i bean che implementano PasswordRule
    private final List<PasswordRule> rules;

    public PasswordHintService(List<PasswordRule> rules) {
        this.rules = rules;
    }

    public List<PasswordHint> getHints(String password) {
        List<PasswordHint> hints = new ArrayList<>();
        if (password == null) return hints;

        for (PasswordRule rule : rules) {
            rule.check(password).ifPresent(hints::add);
        }
        return hints;
    }
}