package com.cromerosi.homomorfismos.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Implementación del esquema de cifrado homomórfico Paillier.
 */
public class Paillier {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PaillierPublicKey publicKey;
    private final PaillierPrivateKey privateKey;
    private final BigInteger nBigInteger; // n = p * q
    private final BigInteger nSquareBigInteger; // n^2

    public Paillier(int keySize) {

        long p = generatePrime(keySize); // primo grande

        // Buscar un primo distinto a p
        long q = BigInteger.valueOf(p)
                .nextProbablePrime()
                .longValueExact();

        long n = Math.multiplyExact(p, q);
        long lambda = lcm(p - 1, q - 1); // lambda = lcm(p-1, q-1)
        long g = n + 1; // g = n + 1

        this.nBigInteger = BigInteger.valueOf(n);
        this.nSquareBigInteger = nBigInteger.multiply(nBigInteger);

        BigInteger gBigInteger = BigInteger.valueOf(g);
        BigInteger lambdaBigInteger = BigInteger.valueOf(lambda);

        BigInteger lValue = gBigInteger
                .modPow(lambdaBigInteger, nSquareBigInteger)
                .subtract(BigInteger.ONE)
                .divide(nBigInteger);

        long mu = lValue.modInverse(nBigInteger).longValueExact();

        this.publicKey = new PaillierPublicKey(
                n,
                g,
                Math.multiplyExact(n, n)
        );

        this.privateKey = new PaillierPrivateKey(
                lambda,
                mu
        );
    }


    public PaillierPublicKey getPublicKey() {
        return publicKey;
    }

    public PaillierPrivateKey getPrivateKey() {
        return privateKey;
    }

    public long encrypt(long plaintext) {
        long normalizedPlaintext = Math.floorMod(plaintext, publicKey.n);
        long randomR = sampleCoprime(publicKey.n); // r debe ser coprimo con n

        BigInteger term1 = BigInteger.valueOf(publicKey.g)
                .modPow(BigInteger.valueOf(normalizedPlaintext), nSquareBigInteger);
        BigInteger term2 = BigInteger.valueOf(randomR)
                .modPow(BigInteger.valueOf(publicKey.n), nSquareBigInteger);

        return term1.multiply(term2).mod(nSquareBigInteger).longValueExact();
    }

    public long decrypt(long ciphertext) {
        BigInteger ciphertextBigInteger = BigInteger.valueOf(Math.floorMod(ciphertext, publicKey.nSquare));
        BigInteger lValue = ciphertextBigInteger
                .modPow(BigInteger.valueOf(privateKey.lambda), nSquareBigInteger)
                .subtract(BigInteger.ONE)
                .divide(nBigInteger);

        return lValue.multiply(BigInteger.valueOf(privateKey.mu))
                .mod(nBigInteger)
                .longValueExact();
    }

    public long homomorphicAdd(long c1, long c2) {
        BigInteger result = BigInteger.valueOf(Math.floorMod(c1, publicKey.nSquare))
                .multiply(BigInteger.valueOf(Math.floorMod(c2, publicKey.nSquare)))
                .mod(nSquareBigInteger);
        return result.longValueExact();
    }

    public long scalarMultiply(long ciphertext, long scalar) {
        if (scalar < 0) {
            throw new IllegalArgumentException("El escalar debe ser no negativo");
        }

        BigInteger result = BigInteger.valueOf(Math.floorMod(ciphertext, publicKey.nSquare))
                .modPow(BigInteger.valueOf(scalar), nSquareBigInteger);
        return result.longValueExact();
    }

    private static long generatePrime(int seed) {
        long base = 40_000L + Math.floorMod(seed, 5_000);

        return BigInteger.valueOf(base)
                .nextProbablePrime()
                .longValueExact();
    }
        
    private static long sampleCoprime(long modulus) {
        long candidate;
        do {
            candidate = 1 + Math.floorMod(RANDOM.nextLong(), modulus - 1);
        } while (!BigInteger.valueOf(candidate).gcd(BigInteger.valueOf(modulus)).equals(BigInteger.ONE));
        return candidate;
    }

    private static long lcm(long a, long b) {
        return Math.multiplyExact(a / gcd(a, b), b);
    }

    private static long gcd(long a, long b) {
        while (b != 0) {
            long temp = b;
            b = a % b;
            a = temp;
        }
        return Math.abs(a);
    }

    public static final class PaillierPublicKey {

        private final long n; // n = p * q
        private final long g; // generador publico
        private final long nSquare; // n^2

        private PaillierPublicKey(long n, long g, long nSquare) {
            this.n = n;
            this.g = g;
            this.nSquare = nSquare;
        }

        public long getN() {
            return n;
        }

        public long getG() {
            return g;
        }

        public long getNSquare() {
            return nSquare;
        }
    }

    public static final class PaillierPrivateKey {

        private final long lambda; // lambda = lcm(p-1, q-1)
        private final long mu; // mu = L(g^lambda mod n^2)^(-1) mod n

        private PaillierPrivateKey(long lambda, long mu) {
            this.lambda = lambda;
            this.mu = mu;
        }

        public long getLambda() {
            return lambda;
        }

        public long getMu() {
            return mu;
        }
    }
}
