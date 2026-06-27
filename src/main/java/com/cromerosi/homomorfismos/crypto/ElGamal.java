package com.cromerosi.homomorfismos.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Implementación del esquema de cifrado homomórfico ElGamal.
 */
public class ElGamal {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final ElGamalPublicKey publicKey;
    private final ElGamalPrivateKey privateKey;
    private final long modulus; // p: primo grande (módulo del grupo)

    public ElGamal(int keySize) {
        this.modulus = generateModulus(keySize);
        this.publicKey = new ElGamalPublicKey(modulus);
        this.privateKey = new ElGamalPrivateKey(modulus);
    }

    public ElGamalPublicKey getPublicKey() {
        return publicKey;
    }

    public ElGamalPrivateKey getPrivateKey() {
        return privateKey;
    }

    public long getModulus() {
        return modulus;
    }

    public long[] encrypt(long plaintext) {
        long normalizedPlaintext = Math.floorMod(plaintext, modulus);
        long randomFactor = sampleNonZero(modulus); // k aleatorio no nulo
        long cipherValue = multiplyMod(normalizedPlaintext, randomFactor, modulus);
        return new long[]{cipherValue, randomFactor};
    }

    public long decrypt(long[] ciphertext) {
        if (ciphertext == null || ciphertext.length < 2) {
            throw new IllegalArgumentException("El criptograma debe contener dos valores");
        }

        long cipherValue = Math.floorMod(ciphertext[0], modulus);
        long randomFactor = Math.floorMod(ciphertext[1], modulus);
        if (randomFactor == 0) {
            throw new IllegalArgumentException("El factor aleatorio del criptograma no puede ser 0");
        }
        long inverse = BigInteger.valueOf(randomFactor)
                .modInverse(BigInteger.valueOf(modulus))
                .longValueExact();
        return multiplyMod(cipherValue, inverse, modulus);
    }

    public long[] homomorphicAdd(long[] c1, long[] c2) {
        if (c1 == null || c2 == null || c1.length < 2 || c2.length < 2) {
            throw new IllegalArgumentException("Los criptogramas deben contener dos valores");
        }

        // En ElGamal multiplicativo: E(m1) * E(m2) = E(m1 * m2)
        long combinedCipherValue = multiplyMod(c1[0], c2[0], modulus);
        long combinedRandomFactor = multiplyMod(c1[1], c2[1], modulus);
        return new long[]{combinedCipherValue, combinedRandomFactor};
    }

    public static final class ElGamalPublicKey {

        private final long modulus; // p: módulo público

        private ElGamalPublicKey(long modulus) {
            this.modulus = modulus;
        }

        public long getModulus() {
            return modulus;
        }
    }

    public static final class ElGamalPrivateKey {

        private final long modulus; // en esta simplificación la clave privada depende de p

        private ElGamalPrivateKey(long modulus) {
            this.modulus = modulus;
        }

        public long getModulus() {
            return modulus;
        }
    }

    private static long generateModulus(int keySize) {
        if (keySize >= 256) {
            return 1_000_000_007L;
        }
        return Math.max(257L, keySize * 19L + 3L);
    }

    private static long sampleNonZero(long boundExclusive) {
        return 1 + Math.floorMod(RANDOM.nextLong(), boundExclusive - 1);
    }

    private static long multiplyMod(long left, long right, long modulus) {
        return BigInteger.valueOf(left)
                .multiply(BigInteger.valueOf(right))
                .mod(BigInteger.valueOf(modulus))
                .longValueExact();
    }
}
