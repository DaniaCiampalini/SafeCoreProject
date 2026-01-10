package com.safecore.business.service;

import com.safecore.persistence.entity.EmailAliasEntity;
import com.safecore.persistence.entity.UserEntity;
import com.safecore.persistence.repository.EmailAliasRepository;
import com.safecore.persistence.repository.UserRepository;
import com.safecore.ui.session.SessionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Service
public class EmailAliasService {

    private static final String[] ADJECTIVES = {"swift", "secure", "silent", "brave", "cool", "hidden", "ghost"};
    private static final String[] NOUNS = {"vault", "core", "shield", "key", "safe", "lock", "entry"};
    private static final String DOMAIN = "safecore.io";
    
    private final EmailAliasRepository emailAliasRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public EmailAliasService(EmailAliasRepository emailAliasRepository, UserRepository userRepository) {
        this.emailAliasRepository = emailAliasRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String generateAlias(String serviceName) {
        String email = SessionContext.getCurrentUserEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        String cleanService = serviceName.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (cleanService.isEmpty()) cleanService = "service";
        
        String adjective = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[random.nextInt(NOUNS.length)];
        int number = 1000 + random.nextInt(9000);

        String alias = String.format("%s.%s.%s%d@%s", cleanService, adjective, noun, number, DOMAIN);

        EmailAliasEntity entity = new EmailAliasEntity();
        entity.setAliasEmail(alias);
        entity.setServiceName(serviceName);
        entity.setUser(user);

        emailAliasRepository.save(entity);
        return alias;
    }

    public List<EmailAliasEntity> getAliasesForCurrentUser() {
        return emailAliasRepository.findByUserEmail(SessionContext.getCurrentUserEmail());
    }
}
