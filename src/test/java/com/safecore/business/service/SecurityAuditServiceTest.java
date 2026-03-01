package com.safecore.business.service;

import com.safecore.business.domain.AuditResult;
import com.safecore.business.service.impl.SecurityAuditServiceImpl;
import com.safecore.persistence.entity.PasswordEntryEntity;
import com.safecore.security.PasswordStrengthEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test di unità per SecurityAuditService.
 * Verifica il calcolo dello Health Score e la rilevazione delle vulnerabilità.
 *
 * Algoritmo di scoring (percentuale):
 * - Vault vuoto = 100/100
 * - weakPenalty = (weak/total * 100) * 0.5   (50% se tutte deboli)
 * - oldPenalty = (old/total * 100) * 0.25     (25% se tutte vecchie)
 * - reusedPenalty = (reused/total * 100) * 0.25 (25% se tutte replicate)
 * - Score finale = 100 - weakPenalty - oldPenalty - reusedPenalty (min 0)
 */
class SecurityAuditServiceTest {

    private VaultService vaultService;
    private PasswordStrengthEvaluator evaluator;
    private SecurityAuditService auditService;

    @BeforeEach
    void setUp() {
        vaultService = mock(VaultService.class);
        evaluator = mock(PasswordStrengthEvaluator.class);
        auditService = new SecurityAuditServiceImpl(vaultService, evaluator);
    }

    @Test
    @DisplayName("Audit su Vault vuoto: deve restituire punteggio perfetto")
    void runAudit_emptyVault_returnsPerfectScore() {
        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of());

        AuditResult result = auditService.runAudit();

        assertAll(
                () -> assertEquals(100, result.score(), "Il punteggio iniziale deve essere 100"),
                () -> assertEquals(0, result.totalPasswords(), "Il totale deve essere zero")
        );
    }

    @Test
    @DisplayName("Rilevamento Password Debole: verifica penalità score")
    void runAudit_detectsWeakPassword() {
        PasswordEntryEntity entry = mockEntry("weak_pwd", LocalDateTime.now());

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(entry));
        when(vaultService.decryptPassword(any())).thenReturn("123");
        when(evaluator.evaluate("123")).thenReturn(PasswordStrengthEvaluator.Strength.WEAK);

        AuditResult result = auditService.runAudit();

        assertEquals(1, result.weakCount());
        // 1 su 1 = 100% debole -> penalità = 100% * 0.5 = 50% -> score = 100 - 50 = 50.0
        assertEquals(50.0, result.score(), "Una password debole (100%) deve dare score 50.0");
    }

    @Test
    @DisplayName("Rilevamento Password Vecchia: verifica penalità per password obsoleta")
    void runAudit_detectsOldPassword() {
        // Password creata 1 anni fa
        PasswordEntryEntity entry = mockEntry("old_pwd", LocalDateTime.now().minusYears(1));

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(entry));
        when(vaultService.decryptPassword(any())).thenReturn("StrongPass!2022");
        when(evaluator.evaluate(any())).thenReturn(PasswordStrengthEvaluator.Strength.STRONG);

        AuditResult result = auditService.runAudit();

        assertEquals(1, result.oldCount());
        // 1 su 1 = 100% vecchia -> penalità = 100% * 0.25 = 25% -> score = 100 - 25 = 75.0
        assertEquals(75.0, result.score(), "Una password vecchia (100%) deve dare score 75.0");
    }

    @Test
    @DisplayName("Rilevamento Password Riutilizzate: verifica penalità multipla")
    void runAudit_detectsReusedPasswords() {
        // Due entry diverse con la stessa password decifrata
        PasswordEntryEntity e1 = mockEntry("p1", LocalDateTime.now());
        PasswordEntryEntity e2 = mockEntry("p2", LocalDateTime.now());

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(e1, e2));
        when(vaultService.decryptPassword(any())).thenReturn("IdenticalPassword123!");
        when(evaluator.evaluate(any())).thenReturn(PasswordStrengthEvaluator.Strength.STRONG);

        AuditResult result = auditService.runAudit();

        assertEquals(2, result.reusedCount(), "Entrambe le entry devono essere segnate come riutilizzate");
        // 2 su 2 = 100% riutilizzate -> penalità = 100% * 0.25 = 25% -> score = 100 - 25 = 75.0
        assertEquals(75.0, result.score(), "Due password riutilizzate (100%) devono dare score 75.0");
    }

    @Test
    @DisplayName("Caso Combinato: Password debole, vecchia e riutilizzata")
    void runAudit_complexScenario() {
        // Scenario peggiore: 2 password identiche, entrambe deboli e vecchie
        PasswordEntryEntity e1 = mockEntry("p1", LocalDateTime.now().minusYears(2));
        PasswordEntryEntity e2 = mockEntry("p2", LocalDateTime.now().minusYears(2));

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(e1, e2));
        when(vaultService.decryptPassword(any())).thenReturn("abc");
        when(evaluator.evaluate("abc")).thenReturn(PasswordStrengthEvaluator.Strength.WEAK);

        AuditResult result = auditService.runAudit();

        // 2 su 2 = 100% per ogni categoria
        // Penalità: weak(100% * 0.5) + old(100% * 0.25) + reused(100% * 0.25) = 50 + 25 + 25 = 100
        // Score: 100 - 100 = 0.0
        assertEquals(0.0, result.score(), "Caso peggiore (tutto 100%) deve dare score 0.0");
        assertEquals(2, result.weakCount());
        assertEquals(2, result.reusedCount());
        assertEquals(2, result.oldCount());
    }

    @Test
    @DisplayName("Floor Score: Il punteggio non deve mai scendere sotto zero")
    void runAudit_scoreNeverBelowZero() {
        // Simuliamo tantissime violazioni
        List<PasswordEntryEntity> manyEntries = List.of(
                mockEntry("p1", LocalDateTime.now()),
                mockEntry("p2", LocalDateTime.now()),
                mockEntry("p3", LocalDateTime.now()),
                mockEntry("p4", LocalDateTime.now()),
                mockEntry("p5", LocalDateTime.now())
        );

        when(vaultService.getEntriesForCurrentUser()).thenReturn(manyEntries);
        when(vaultService.decryptPassword(any())).thenReturn("123");
        when(evaluator.evaluate(any())).thenReturn(PasswordStrengthEvaluator.Strength.WEAK);

        AuditResult result = auditService.runAudit();

        assertTrue(result.score() >= 0, "Lo score deve essere normalizzato a un minimo di 0");
    }

    // -------- Helpers --------

    private PasswordEntryEntity mockEntry(String pwd, LocalDateTime createdAt) {
        PasswordEntryEntity entry = mock(PasswordEntryEntity.class);
        when(entry.getEncryptedPassword()).thenReturn(pwd.getBytes());
        when(entry.getCreatedAt()).thenReturn(createdAt);
        when(entry.getServiceName()).thenReturn("Service_" + pwd);
        return entry;
    }
}