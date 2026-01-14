package com.safecore.security;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EncryptionFactory {

    private final Map<String, EncryptionStrategy> strategies;

    public EncryptionFactory(List<EncryptionStrategy> strategyList) {
        // Mappiamo le strategie in base al loro nome semplice della classe o un identificatore
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        s -> s.getClass().getSimpleName().replace("EncryptionStrategy", "").toUpperCase(),
                        Function.identity()
                ));
    }

    public EncryptionStrategy getStrategy(String type) {
        EncryptionStrategy strategy = strategies.get(type.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported encryption strategy: " + type);
        }
        return strategy;
    }

    public EncryptionStrategy getDefaultStrategy() {
        return getStrategy("AES");
    }
}
