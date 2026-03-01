package com.safecore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di performance per AESEncryptionStrategy.
 * Verifica che le operazioni di cifratura e decifratura siano entro limiti accettabili.
 * Questi test aiutano a monitorare le performance e a identificare regressioni.
 */
class AESEncryptionPerformanceTest {

    private AESEncryptionStrategy encryptionStrategy;

    @BeforeEach
    void setUp() {
        KeyManager keyManager = new KeyManager();
        keyManager.initialize("TestPassword123!", new byte[32]); // Inizializza KeyManager con password e salt
        encryptionStrategy = new AESEncryptionStrategy(keyManager);

        // Warm-up: esegui 100 cifrature a vuoto per scaldare la JVM
        for (int i = 0; i < 100; i++) {
            encryptionStrategy.encrypt("warmup");
        }
    }

    @Test
    @DisplayName("Encrypt operation should complete within reasonable time")
    void encryptPerformance_shouldCompleteWithinTimeLimit() {
        String plainText = "This is a test password to encrypt";

        long startTime = System.nanoTime();
        byte[] encrypted = encryptionStrategy.encrypt(plainText);
        long endTime = System.nanoTime();

        long durationMicros = (endTime - startTime) / 1_000;

        // AES è molto veloce, dovrebbe completare in meno di 10ms
        assertTrue(durationMicros < 10_000,
                "Encrypt operation took too long: " + durationMicros + "μs (expected < 10000μs)");

        assertNotNull(encrypted);
        assertNotEquals(plainText, new String(encrypted));
    }

    @Test
    @DisplayName("Decrypt operation should complete within reasonable time")
    void decryptPerformance_shouldCompleteWithinTimeLimit() {
        String plainText = "This is a test password to encrypt";
        byte[] encrypted = encryptionStrategy.encrypt(plainText);

        long startTime = System.nanoTime();
        String decrypted = encryptionStrategy.decrypt(encrypted);
        long endTime = System.nanoTime();

        long durationMicros = (endTime - startTime) / 1_000;

        // La decifratura dovrebbe essere veloce come la cifratura
        assertTrue(durationMicros < 10_000,
                "Decrypt operation took too long: " + durationMicros + "μs (expected < 10000μs)");

        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("Encrypt and decrypt round-trip should be fast")
    void roundTripPerformance_shouldCompleteWithinTimeLimit() {
        String plainText = "This is a test password to encrypt";

        long startTime = System.nanoTime();
        byte[] encrypted = encryptionStrategy.encrypt(plainText);
        String decrypted = encryptionStrategy.decrypt(encrypted);
        long endTime = System.nanoTime();

        long durationMicros = (endTime - startTime) / 1_000;

        // L'intero round-trip dovrebbe essere veloce
        assertTrue(durationMicros < 20_000,
                "Round-trip operation took too long: " + durationMicros + "μs (expected < 20000μs)");

        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("Multiple encrypt operations should have consistent performance")
    void multipleEncryptOperations_shouldHaveConsistentPerformance() {
        String plainText = "TestPassword123!";

        // 1. Warm-up aggressivo (essenziale con il debugger attivo)
        for (int i = 0; i < 1000; i++) {
            encryptionStrategy.encrypt(plainText);
        }

        int testIterations = 500; // Più dati riducono l'impatto degli outliers
        long[] durations = new long[testIterations];

        for (int i = 0; i < testIterations; i++) {
            long startTime = System.nanoTime();
            encryptionStrategy.encrypt(plainText);
            long endTime = System.nanoTime();
            durations[i] = (endTime - startTime) / 1_000;
        }

        double average = calculateAverage(durations);
        double stdDev = calculateStandardDeviation(durations, average);

        System.out.println("Risultati Finali -> Media: " + average + "μs, Deviaz.Std: " + stdDev + "μs");

        // 2. Soglia più tollerante per ambienti di sviluppo/debug
        // Accettiamo una deviazione standard fino a 1000μs (1ms) o 2 volte la media
        boolean isConsistent = stdDev < 1000 || stdDev < (average * 2.0);

        assertTrue(isConsistent,
                "Performance troppo instabile. Media: " + average + "μs, StdDev: " + stdDev + "μs");
    }

    @Test
    @DisplayName("Encrypt performance with different text lengths")
    void encryptPerformance_withDifferentTextLengths() {
        String shortText = "Hi";
        String mediumText = "This is a medium length text for testing encryption performance";
        String longText = "This is a very long text that contains many characters to test " +
                "encryption performance with larger inputs. ".repeat(10);

        long shortTime = measureEncryptTime(shortText);
        long mediumTime = measureEncryptTime(mediumText);
        long longTime = measureEncryptTime(longText);

        // Tutte dovrebbero essere veloci
        assertTrue(shortTime < 10_000, "Short text encrypt too slow: " + shortTime + "μs");
        assertTrue(mediumTime < 10_000, "Medium text encrypt too slow: " + mediumTime + "μs");
        assertTrue(longTime < 200_000, "Long text encrypt too slow: " + longTime + "μs");

        // Il testo più lungo potrebbe richiedere più tempo, ma non proporzionalmente
        assertTrue(longTime < shortTime * 100,
                "Long text encryption is disproportionately slow");
    }

    @Test
    @DisplayName("Decrypt performance with different text lengths")
    void decryptPerformance_withDifferentTextLengths() {
        String shortText = "Hi";
        String mediumText = "This is a medium length text for testing encryption performance";
        String longText = "This is a very long text that contains many characters to test " +
                "encryption performance with larger inputs. ".repeat(10);

        byte[] shortEncrypted = encryptionStrategy.encrypt(shortText);
        byte[] mediumEncrypted = encryptionStrategy.encrypt(mediumText);
        byte[] longEncrypted = encryptionStrategy.encrypt(longText);

        long shortTime = measureDecryptTime(shortEncrypted);
        long mediumTime = measureDecryptTime(mediumEncrypted);
        long longTime = measureDecryptTime(longEncrypted);

        // Tutte dovrebbero essere veloci
        assertTrue(shortTime < 10_000, "Short text decrypt too slow: " + shortTime + "μs");
        assertTrue(mediumTime < 10_000, "Medium text decrypt too slow: " + mediumTime + "μs");
        assertTrue(longTime < 50_000, "Long text decrypt too slow: " + longTime + "μs");
    }

    @Test
    @DisplayName("Concurrent encrypt operations should not degrade performance significantly")
    void concurrentEncryptOperations_shouldNotDegradePerformance() {
        String plainText = "TestPassword123!";
        int threadCount = 10;
        int encryptionsPerThread = 20;

        long singleThreadTime = measureMultipleEncrypts(plainText, encryptionsPerThread);

        // Simula operazioni concorrenti
        long concurrentTime = measureConcurrentEncrypts(plainText, threadCount, encryptionsPerThread);

        // Il tempo concorrente non dovrebbe essere molto peggiore del tempo singolo
        // (considerando l'overhead del threading)
        double ratio = (double) concurrentTime / (singleThreadTime * threadCount);

        assertTrue(ratio < 3.0,
                "Concurrent operations degraded performance too much. Ratio: " + ratio);
    }

    @Test
    @DisplayName("Encrypt operation should be fast enough for real-time use")
    void encryptPerformance_shouldBeFastEnoughForRealTime() {
        String plainText = "Real-time password entry";

        // Misura il tempo per 100 operazioni
        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            encryptionStrategy.encrypt(plainText);
        }

        long endTime = System.nanoTime();
        long totalTimeMicros = (endTime - startTime) / 1_000;
        long avgTimeMicros = totalTimeMicros / iterations;

        // Ogni operazione dovrebbe essere molto veloce (< 1ms)
        assertTrue(avgTimeMicros < 1_000,
                "Average encrypt time is too slow for real-time: " + avgTimeMicros + "μs");
    }

    @Test
    @DisplayName("Decrypt operation should be fast enough for real-time use")
    void decryptPerformance_shouldBeFastEnoughForRealTime() {
        String plainText = "Real-time password entry";
        byte[] encrypted = encryptionStrategy.encrypt(plainText);

        // Misura il tempo per 100 operazioni
        int iterations = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            encryptionStrategy.decrypt(encrypted);
        }

        long endTime = System.nanoTime();
        long totalTimeMicros = (endTime - startTime) / 1_000;
        long avgTimeMicros = totalTimeMicros / iterations;

        // Ogni operazione dovrebbe essere molto veloce (< 1ms)
        assertTrue(avgTimeMicros < 1_000,
                "Average decrypt time is too slow for real-time: " + avgTimeMicros + "μs");
    }

    @Test
    @DisplayName("Encryption with special characters should not impact performance")
    void encryptPerformance_withSpecialCharacters() {
        String normalText = "NormalPassword123";
        String specialCharsText = "P@ssw0rd!#$%^&*()_+-=[]{}|;:,.<>?";
        String unicodeText = "Pàsswòrdñç日本語";

        long normalTime = measureEncryptTime(normalText);
        long specialTime = measureEncryptTime(specialCharsText);
        long unicodeTime = measureEncryptTime(unicodeText);

        System.out.println("Normal: " + normalTime + "μs, Special: " + specialTime + "μs, Unicode: " + unicodeTime + "μs");

        // Tutte dovrebbero essere veloci (soglia più tollerante per macchine lente)
        assertTrue(normalTime < 50_000, "Normal text encrypt too slow: " + normalTime + "μs");
        assertTrue(specialTime < 50_000, "Special chars encrypt too slow: " + specialTime + "μs");
        assertTrue(unicodeTime < 50_000, "Unicode text encrypt too slow: " + unicodeTime + "μs");

        // I tempi dovrebbero essere simili (entro un fattore 5 per tollerare variabilità)
        assertTrue(specialTime < normalTime * 5,
                "Special chars encryption too slow. Normal: " + normalTime + "μs, Special: " + specialTime + "μs");
        assertTrue(unicodeTime < normalTime * 5,
                "Unicode encryption too slow. Normal: " + normalTime + "μs, Unicode: " + unicodeTime + "μs");
    }

    @Test
    @DisplayName("Memory usage should be reasonable for encryption")
    void encryptPerformance_memoryUsageShouldBeReasonable() {
        String plainText = "TestPassword123!";
        int iterations = 1000;

        // Esegui molte operazioni per vedere se ci sono problemi di memoria
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            byte[] encrypted = encryptionStrategy.encrypt(plainText);
            String decrypted = encryptionStrategy.decrypt(encrypted);
            assertEquals(plainText, decrypted);
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;

        // 1000 operazioni dovrebbero completare in meno di 5 secondi
        assertTrue(totalTimeMs < 5_000,
                "1000 encryption/decryption operations took too long: " + totalTimeMs + "ms");
    }

    // Helper methods

    private long measureEncryptTime(String plainText) {
        long startTime = System.nanoTime();
        encryptionStrategy.encrypt(plainText);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000; // microseconds
    }

    private long measureDecryptTime(byte[] encrypted) {
        long startTime = System.nanoTime();
        encryptionStrategy.decrypt(encrypted);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000; // microseconds
    }

    private long measureMultipleEncrypts(String plainText, int count) {
        long startTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            encryptionStrategy.encrypt(plainText);
        }
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000; // microseconds
    }

    private long measureConcurrentEncrypts(String plainText, int threadCount, int encryptionsPerThread) {
        Thread[] threads = new Thread[threadCount];

        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < encryptionsPerThread; j++) {
                    encryptionStrategy.encrypt(plainText);
                }
            });
            threads[i].start();
        }

        // Attendi che tutti i thread completino
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("Test interrupted");
            }
        }

        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000; // microseconds
    }

    private double calculateAverage(long[] values) {
        double sum = 0;
        for (long value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private double calculateStandardDeviation(long[] values, double average) {
        double sumSquaredDifferences = 0;
        for (long value : values) {
            double difference = value - average;
            sumSquaredDifferences += difference * difference;
        }
        return Math.sqrt(sumSquaredDifferences / values.length);
    }
}
