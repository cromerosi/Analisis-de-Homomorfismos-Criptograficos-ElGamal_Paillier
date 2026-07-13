package com.cromerosi.homomorfismos.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ElGamal {
    private final BigInteger p;
    private final BigInteger g;
    private final BigInteger x; // Clave privada
    private final BigInteger y; // Clave pública

    public ElGamal(int bitLength) {
        SecureRandom random = new SecureRandom();
        this.p = BigInteger.probablePrime(bitLength, random);
        this.g = findGenerator(p);
        this.x = new BigInteger(bitLength - 1, random).mod(p.subtract(BigInteger.ONE));
        this.y = g.modPow(x, p);
    }

    private BigInteger findGenerator(BigInteger p) {
        BigInteger pMinusOne = p.subtract(BigInteger.ONE);
        BigInteger two = BigInteger.valueOf(2);
        for (BigInteger gen = two; gen.compareTo(pMinusOne) < 0; gen = gen.add(BigInteger.ONE)) {
            if (gen.modPow(pMinusOne.divide(two), p).compareTo(BigInteger.ONE) != 0) {
                return gen;
            }
        }
        return two; // Fallback básico
    }

    public BigInteger[] encrypt(BigInteger m) {
        SecureRandom random = new SecureRandom();
        BigInteger k = new BigInteger(p.bitLength() - 1, random).mod(p.subtract(BigInteger.ONE));
        if (k.equals(BigInteger.ZERO)) k = BigInteger.ONE;
        
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

    public BigInteger getP() { return p; }
    public BigInteger getG() { return g; }
    public BigInteger getY() { return y; }
}
