package com.cromerosi.homomorfismos;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class Paillier {

    private final BigInteger p; // primo grande
    private final BigInteger q; // primo grande
    private final BigInteger n; // n = p * q
    private final BigInteger g; // generador
    private final BigInteger lambda; // λ = lcm(p-1, q-1)
    private final BigInteger mu; // μ = (L(g^λ mod n^2))^(-1) mod n

    public Paillier(int bitLength) {
        SecureRandom random = new SecureRandom();
        this.p = BigInteger.probablePrime(bitLength / 2, random);
        this.q = BigInteger.probablePrime(bitLength / 2, random);
        this.n = p.multiply(q);
        this.g = n.add(BigInteger.ONE); // g = n + 1
        this.lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        this.mu = g.modPow(lambda, n.multiply(n)).subtract(BigInteger.ONE).divide(n).modInverse(n);
    }

    private BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b));
    }

    public BigInteger encrypt(BigInteger m) {
        Random random = new SecureRandom();
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength(), random).mod(n);
        } while (r.equals(BigInteger.ZERO)); // r debe ser coprimo con n
        return g.modPow(m, n.multiply(n)).multiply(r.modPow(n, n.multiply(n))).mod(n.multiply(n));
    }

    public BigInteger decrypt(BigInteger c) {
        BigInteger u = c.modPow(lambda, n.multiply(n)).subtract(BigInteger.ONE).divide(n).multiply(mu).mod(n);
        return u;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getG() {
        return g;
    }
}