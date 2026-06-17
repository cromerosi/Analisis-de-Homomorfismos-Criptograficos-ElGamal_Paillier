package com.cromerosi.homomorfismos.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Cifrado ElGamal")
class ElGamalTest {

    private ElGamal elGamal;

    @BeforeEach
    void setUp() {
        // Inicializar ElGamal con parámetros pequeños para pruebas rápidas
        elGamal = new ElGamal(512);
    }

    @Test
    @DisplayName("Generar claves ElGamal")
    void testKeyGeneration() {
        assertNotNull(elGamal.getPublicKey());
        assertNotNull(elGamal.getPrivateKey());
    }

    @Test
    @DisplayName("Cifrado y descifrado consistentes")
    void testEncryptDecrypt() {
        long plaintext = 42;
        long[] ciphertext = elGamal.encrypt(plaintext);
        assertNotNull(ciphertext);
        assertTrue(ciphertext.length >= 2);
        
        long decrypted = elGamal.decrypt(ciphertext);
        assertEquals(plaintext, decrypted);
    }

    @Test
    @DisplayName("Propiedad homomórfica aditiva de ElGamal")
    void testHomomorphicAddition() {
        long m1 = 10;
        long m2 = 20;
        
        long[] c1 = elGamal.encrypt(m1);
        long[] c2 = elGamal.encrypt(m2);
        
        long[] c_sum = elGamal.homomorphicAdd(c1, c2);
        long decrypted = elGamal.decrypt(c_sum);
        
        // En ElGamal multiplicativo: E(m1) * E(m2) = E(m1 * m2)
        assertEquals((m1 * m2) % elGamal.getModulus(), decrypted);
    }

    @Test
    @DisplayName("El texto cifrado es diferente al original")
    void testCiphertextDifferent() {
        long plaintext = 5;
        long[] ciphertext = elGamal.encrypt(plaintext);
        assertNotEquals(plaintext, ciphertext[0]);
        assertNotEquals(plaintext, ciphertext[1]);
    }

    @Test
    @DisplayName("Textos planos diferentes generan criptogramas diferentes")
    void testDifferentPlaintextsDifferentCiphertext() {
        long m1 = 7;
        long m2 = 13;
        
        long[] c1 = elGamal.encrypt(m1);
        long[] c2 = elGamal.encrypt(m2);
        
        assertFalse(java.util.Arrays.equals(c1, c2));
    }
}
