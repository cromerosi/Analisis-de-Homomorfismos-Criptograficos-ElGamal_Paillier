package com.cromerosi.homomorfismos.benchmark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.cromerosi.homomorfismos.crypto.ElGamal;
import com.cromerosi.homomorfismos.crypto.Paillier;

@DisplayName("Pruebas de Rendimiento y Comparativa")
class BenchmarkTest {

    private PerformanceMetrics metrics;
    private ElGamal elGamal;
    private Paillier paillier;

    @BeforeEach
    void setUp() {
        System.out.println("Creando PerformanceMetrics");
        metrics = new PerformanceMetrics();

        System.out.println("Creando ElGamal");
        elGamal = new ElGamal(512);
        System.out.println("ElGamal OK");

        System.out.println("Creando Paillier");
        paillier = new Paillier(512);
        System.out.println("Paillier OK");
    }

    @Test
    @DisplayName("Medir tiempo de cifrado ElGamal")
    void testElGamalEncryptionSpeed() {
        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            elGamal.encrypt(i % 2);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordElGamalEncryptionTime(avgTime));
    }

    @Test
    @DisplayName("Medir tiempo de cifrado Paillier")
    void testPaillierEncryptionSpeed() {
        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            paillier.encrypt(i % 2);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordPaillierEncryptionTime(avgTime));
    }

    @Test
    @DisplayName("Medir tiempo de descifrado ElGamal")
    void testElGamalDecryptionSpeed() {
        long[] ciphertext = elGamal.encrypt(1);

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            elGamal.decrypt(ciphertext);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordElGamalDecryptionTime(avgTime));
    }

    @Test
    @DisplayName("Medir tiempo de descifrado Paillier")
    void testPaillierDecryptionSpeed() {
        long ciphertext = paillier.encrypt(1);

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            paillier.decrypt(ciphertext);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordPaillierDecryptionTime(avgTime));
    }

    @Test
    @DisplayName("Medir operación homomórfica ElGamal")
    void testElGamalHomomorphicSpeed() {
        long[] c1 = elGamal.encrypt(1);
        long[] c2 = elGamal.encrypt(1);

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            elGamal.homomorphicAdd(c1, c2);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordElGamalHomomorphicOpTime(avgTime));
    }

    @Test
    @DisplayName("Medir operación homomórfica Paillier")
    void testPaillierHomomorphicSpeed() {
        long c1 = paillier.encrypt(1);
        long c2 = paillier.encrypt(1);

        long startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            paillier.homomorphicAdd(c1, c2);
        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        assertTrue(duration > 0);
        long avgTime = duration / 100;
        assertNotNull(metrics.recordPaillierHomomorphicOpTime(avgTime));
    }

    @Test
    @DisplayName("Comparar eficiencia: cifrado ElGamal vs Paillier")
    void testEncryptionEfficiencyComparison() {
        // Benchmark 100 cifrajes en cada esquema
        long elGamalStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            elGamal.encrypt(i % 2);
        }
        long elGamalTime = System.nanoTime() - elGamalStart;

        long paillierStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            paillier.encrypt(i % 2);
        }
        long paillierTime = System.nanoTime() - paillierStart;

        // Ambos deben completar en tiempo razonable
        assertTrue(elGamalTime > 0);
        assertTrue(paillierTime > 0);

        // Registrar comparativa
        assertTrue(metrics.compareEncryptionSpeed(elGamalTime, paillierTime) >= 0);
    }

    @Test
    @DisplayName("Validar consistencia de métricas")
    void testMetricsConsistency() {
        long[] ciphertext1 = elGamal.encrypt(1);
        long decrypted1 = elGamal.decrypt(ciphertext1);
        assertEquals(1, decrypted1);

        long ciphertext2 = paillier.encrypt(1);
        long decrypted2 = paillier.decrypt(ciphertext2);
        assertEquals(1, decrypted2);

        assertNotNull(metrics);
    }
}
