package com.cromerosi.homomorfismos.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Cifrado Paillier")
class PaillierTest {

    private Paillier paillier;

    @BeforeEach
    void setUp() {
        // Inicializar Paillier con parámetros pequeños para pruebas rápidas
        paillier = new Paillier(512);
    }

    @Test
    @DisplayName("Generar claves Paillier")
    void testKeyGeneration() {
        assertNotNull(paillier.getPublicKey());
        assertNotNull(paillier.getPrivateKey());
    }

    @Test
    @DisplayName("Cifrado y descifrado consistentes")
    void testEncryptDecrypt() {
        long plaintext = 42;
        long ciphertext = paillier.encrypt(plaintext);
        assertNotEquals(plaintext, ciphertext);
        
        long decrypted = paillier.decrypt(ciphertext);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Propiedad homomórfica aditiva de Paillier")
    void testHomomorphicAddition() {
        long m1 = 15;
        long m2 = 25;
        
        long c1 = paillier.encrypt(m1);
        long c2 = paillier.encrypt(m2);
        
        // En Paillier: E(m1) * E(m2) = E(m1 + m2)
        long c_sum = paillier.homomorphicAdd(c1, c2);
        long decrypted = paillier.decrypt(c_sum);
        
        assertEquals(m1 + m2, decrypted);
    }

    @Test
    @DisplayName("Multiplicación escalar homomórfica")
    void testScalarMultiplication() {
        long plaintext = 10;
        long scalar = 3;
        
        long ciphertext = paillier.encrypt(plaintext);
        long c_scaled = paillier.scalarMultiply(ciphertext, scalar);
        long decrypted = paillier.decrypt(c_scaled);
        
        assertEquals(plaintext * scalar, decrypted);
    }

    @Test
    @DisplayName("El texto cifrado es diferente al original")
    void testCiphertextDifferent() {
        long plaintext = 5;
        long ciphertext = paillier.encrypt(plaintext);
        assertNotEquals(plaintext, ciphertext);
    }

    @Test
    @DisplayName("Textos planos diferentes generan criptogramas diferentes")
    void testDifferentPlaintextsDifferentCiphertext() {
        long m1 = 7;
        long m2 = 13;
        
        long c1 = paillier.encrypt(m1);
        long c2 = paillier.encrypt(m2);
        
        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("Decifrado de múltiples cifrados aditivos")
    void testMultipleAdditions() {
        long m1 = 5;
        long m2 = 10;
        long m3 = 15;
        
        long c1 = paillier.encrypt(m1);
        long c2 = paillier.encrypt(m2);
        long c3 = paillier.encrypt(m3);
        
        long c_sum = paillier.homomorphicAdd(c1, paillier.homomorphicAdd(c2, c3));
        long decrypted = paillier.decrypt(c_sum);
        
        assertEquals(m1 + m2 + m3, decrypted);
    }
}
