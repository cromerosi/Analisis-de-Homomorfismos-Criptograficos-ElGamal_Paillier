package com.cromerosi.homomorfismos;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class ElGamal {

    private final BigInteger p; // primo grande
    private final BigInteger g; // generador
    private final BigInteger x; // clave privada
    private final BigInteger y; // clave pública

    public ElGamal(int bitLength) {
        SecureRandom random = new SecureRandom();
        this.p = BigInteger.probablePrime(bitLength, random);
        this.g = findGenerator(p);
        this.x = new BigInteger(bitLength - 1, random).mod(p.subtract(BigInteger.ONE));
        this.y = g.modPow(x, p);
    }

    private BigInteger findGenerator(BigInteger p) {
        BigInteger pMinusOne = p.subtract(BigInteger.ONE);
        for (BigInteger g = BigInteger.TWO; g.compareTo(pMinusOne) < 0; g = g.add(BigInteger.ONE)) {
            if (g.modPow(pMinusOne.divide(BigInteger.TWO), p).compareTo(BigInteger.ONE) != 0) {
                return g;
            }
        }
        throw new IllegalArgumentException("No se encontró un generador para el primo dado.");
    }

    public BigInteger[] encrypt(BigInteger m) {
        Random random = new SecureRandom();
        BigInteger k = new BigInteger(p.bitLength() - 1, random).mod(p.subtract(BigInteger.ONE));
        BigInteger c1 = g.modPow(k, p);
        BigInteger c2 = m.multiply(y.modPow(k, p)).mod(p);
        return new BigInteger[]{c1, c2};
    }

    public BigInteger decrypt(BigInteger[] ciphertext) {
        BigInteger c1 = ciphertext[0];
        BigInteger c2 = ciphertext[1];
        BigInteger s = c1.modPow(x, p);
        return c2.multiply(s.modInverse(p)).mod(p);
    }

    public BigInteger getP() {
        return p;
    }

    public BigInteger getG() {
        return g;
    }

    public BigInteger getY() {
        return y;
    }
}