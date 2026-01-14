package com.safecore.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di performance per PasswordHasher.
 * Verifica che le operazioni di hashing e verifica con BCrypt siano entro limiti accettabili.
 * Questi test aiutano a monitorare le performance e a identificare regressioni.
 */
class PasswordHasherPerformanceTest {

    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new PasswordHasher();
    }

    @Test
    @DisplayName("Hash operation should complete within reasonable time")
    void hashPerformance_shouldCompleteWithinTimeLimit() {
        String password = "TestPassword123!";

        long startTime = System.nanoTime();
        String hash = passwordHasher.hash(password);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // BCrypt con cost factor 12 dovrebbe impiegare tra 50ms e 500ms
        // Questo test verifica che non sia eccessivamente lento
        assertTrue(durationMs < 1000,
                "Hash operation took too long: " + durationMs + "ms (expected < 1000ms)");

        // Verifica anche che l'hash sia valido
        assertNotNull(hash);
        assertNotEquals(password, hash);
    }

    @Test
    @DisplayName("Verify operation should be fast")
    void verifyPerformance_shouldCompleteWithinTimeLimit() {
        String password = "TestPassword123!";
        String hash = passwordHasher.hash(password);

        long startTime = System.nanoTime();
        boolean result = passwordHasher.verify(password, hash);
        long endTime = System.nanoTime();

        long durationMs = (endTime - startTime) / 1_000_000;

        // La verifica dovrebbe essere molto più veloce dell'hashing
        assertTrue(durationMs < 500,
                "Verify operation took too long: " + durationMs + "ms (expected < 500ms)");

        assertTrue(result);
    }

    @Test
    @DisplayName("Multiple hash operations should have consistent performance")
    void multipleHashOperations_shouldHaveConsistentPerformance() {
        String password = "TestPassword123!";
        int iterations = 10;

        long[] durations = new long[iterations];

        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            passwordHasher.hash(password);
            long endTime = System.nanoTime();

            durations[i] = (endTime - startTime) / 1_000_000;
        }

        // Calcola media e deviazione standard
        double average = calculateAverage(durations);
        double stdDev = calculateStandardDeviation(durations, average);

        // La deviazione standard dovrebbe essere ragionevole (< 50% della media)
        assertTrue(stdDev < average * 0.5,
                "Hash operations have inconsistent performance. Average: " + average +
                        "ms, StdDev: " + stdDev + "ms");

        // Tutte le operazioni dovrebbero completare entro un limite ragionevole
        for (long duration : durations) {
            assertTrue(duration < 1000,
                    "Hash operation took too long: " + duration + "ms");
        }
    }

    @Test
    @DisplayName("Hash performance with different password lengths")
    void hashPerformance_withDifferentPasswordLengths() {
        String shortPassword = "Ab1!";
        String mediumPassword = "TestPassword123!";
        String longPassword = "ThisIsAVeryLongPasswordThatContainsManyCharacters123456789!@#$%";

        long shortTime = measureHashTime(shortPassword);
        long mediumTime = measureHashTime(mediumPassword);
        long longTime = measureHashTime(longPassword);

        // BCrypt tronca le password a 72 byte, quindi la lunghezza non dovrebbe influenzare molto
        // ma verifichiamo che tutte siano entro limiti accettabili
        assertTrue(shortTime < 1000, "Short password hash too slow: " + shortTime + "ms");
        assertTrue(mediumTime < 1000, "Medium password hash too slow: " + mediumTime + "ms");
        assertTrue(longTime < 1000, "Long password hash too slow: " + longTime + "ms");
    }

    @Test
    @DisplayName("Verify performance with wrong password should be similar to correct password")
    void verifyPerformance_wrongPasswordShouldBeSimilarToCorrect() {
        String correctPassword = "TestPassword123!";
        String wrongPassword = "WrongPassword456!";
        String hash = passwordHasher.hash(correctPassword);

        long correctTime = measureVerifyTime(correctPassword, hash);
        long wrongTime = measureVerifyTime(wrongPassword, hash);

        // I tempi dovrebbero essere simili (BCrypt deve comunque fare il lavoro completo)
        long difference = Math.abs(correctTime - wrongTime);
        double ratio = (double) difference / Math.max(correctTime, wrongTime);

        assertTrue(ratio < 0.5,
                "Verify times differ too much. Correct: " + correctTime + "ms, Wrong: " + wrongTime + "ms");
    }

    @Test
    @DisplayName("Concurrent hash operations should not degrade performance significantly")
    void concurrentHashOperations_shouldNotDegradePerformance() {
        String password = "TestPassword123!";
        int threadCount = 5;
        int hashesPerThread = 3;

        long singleThreadTime = measureMultipleHashes(password, hashesPerThread);

        // Simula operazioni concorrenti
        long concurrentTime = measureConcurrentHashes(password, threadCount, hashesPerThread);

        // Il tempo concorrente non dovrebbe essere molto peggiore del tempo singolo
        // (considerando l'overhead del threading)
        double ratio = (double) concurrentTime / (singleThreadTime * threadCount);

        assertTrue(ratio < 2.0,
                "Concurrent operations degraded performance too much. Ratio: " + ratio);
    }

    @Test
    @DisplayName("Hash operation should not be too fast (security concern)")
    void hashPerformance_shouldNotBeTooFast() {
        String password = "TestPassword123!";

        long duration = measureHashTime(password);

        // BCrypt con cost factor 12 non dovrebbe essere troppo veloce (< 10ms)
        // altrimenti il cost factor potrebbe essere troppo basso
        assertTrue(duration > 10,
                "Hash operation is too fast: " + duration + "ms. " +
                        "This might indicate the cost factor is too low for security.");
    }

    // Helper methods

    private long measureHashTime(String password) {
        long startTime = System.nanoTime();
        passwordHasher.hash(password);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long measureVerifyTime(String password, String hash) {
        long startTime = System.nanoTime();
        passwordHasher.verify(password, hash);
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long measureMultipleHashes(String password, int count) {
        long startTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            passwordHasher.hash(password);
        }
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000;
    }

    private long measureConcurrentHashes(String password, int threadCount, int hashesPerThread) {
        Thread[] threads = new Thread[threadCount];
        long[] threadTimes = new long[threadCount];

        long startTime = System.nanoTime();

        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                long threadStart = System.nanoTime();
                for (int j = 0; j < hashesPerThread; j++) {
                    passwordHasher.hash(password);
                }
                long threadEnd = System.nanoTime();
                threadTimes[threadIndex] = (threadEnd - threadStart) / 1_000_000;
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
        return (endTime - startTime) / 1_000_000;
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
