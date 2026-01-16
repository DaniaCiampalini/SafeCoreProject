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
 * * Criteri di test:
 * - Vault vuoto = 100/100
 * - Password debole = -10 punti
 * - Password vecchia (> 1 anno) = -5 punti
 * - Password riutilizzata = -15 punti per ogni occorrenza
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
        assertEquals(90, result.score(), "Una password debole deve sottrarre 10 punti");
    }

    @Test
    @DisplayName("Rilevamento Password Vecchia: verifica penalità per obsolescenza")
    void runAudit_detectsOldPassword() {
        // Password creata 2 anni fa
        PasswordEntryEntity entry = mockEntry("old_pwd", LocalDateTime.now().minusYears(2));

        when(vaultService.getEntriesForCurrentUser()).thenReturn(List.of(entry));
        when(vaultService.decryptPassword(any())).thenReturn("StrongPass!2022");
        when(evaluator.evaluate(any())).thenReturn(PasswordStrengthEvaluator.Strength.STRONG);

        AuditResult result = auditService.runAudit();

        assertEquals(1, result.oldCount());
        assertEquals(95, result.score(), "Una password vecchia deve sottrarre 5 punti");
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
        // -15 * 2 = -30 punti. 100 - 30 = 70.
        assertEquals(70, result.score());
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

        // Penalità teorica: (2 * Weak: -20) + (2 * Old: -10) + (2 * Reused: -30) = -60
        // Score atteso: 40
        assertEquals(40, result.score());
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
        return entry;
    }
}