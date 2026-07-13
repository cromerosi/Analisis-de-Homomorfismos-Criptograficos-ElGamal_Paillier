package com.cromerosi.homomorfismos.crypto;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Paillier {
    private final BigInteger n;
    private final BigInteger nSquare;
    private final BigInteger g;
    private final BigInteger lambda;
    private final BigInteger mu;

    public Paillier(int bitLength) {
        SecureRandom random = new SecureRandom();
        BigInteger p = BigInteger.probablePrime(bitLength / 2, random);
        BigInteger q = BigInteger.probablePrime(bitLength / 2, random);
        
        this.n = p.multiply(q);
        this.nSquare = n.multiply(n);
        this.g = n.add(BigInteger.ONE); // g = n + 1
        this.lambda = lcm(p.subtract(BigInteger.ONE), q.subtract(BigInteger.ONE));
        
        // mu = (L(g^lambda mod n^2))^(-1) mod n
        BigInteger l = g.modPow(lambda, nSquare).subtract(BigInteger.ONE).divide(n);
        this.mu = l.modInverse(n);
    }

    private BigInteger lcm(BigInteger a, BigInteger b) {
        return a.multiply(b).divide(a.gcd(b));
    }

    public BigInteger encrypt(BigInteger m) {
        SecureRandom random = new SecureRandom();
        BigInteger r;
        do {
            r = new BigInteger(n.bitLength(), random).mod(n);
        } while (r.equals(BigInteger.ZERO) || !r.gcd(n).equals(BigInteger.ONE)); 
        
        // c = g^m * r^n mod n^2
        BigInteger term1 = g.modPow(m, nSquare);
        BigInteger term2 = r.modPow(n, nSquare);
        return term1.multiply(term2).mod(nSquare);
    }

    public BigInteger decrypt(BigInteger c) {
        // m = L(c^lambda mod n^2) * mu mod n
        BigInteger u = c.modPow(lambda, nSquare).subtract(BigInteger.ONE).divide(n);
        return u.multiply(mu).mod(n);
    }
    
    public BigInteger homomorphicAdd(BigInteger c1, BigInteger c2) {
        // E(m1 + m2) = E(m1) * E(m2) mod n^2
        return c1.multiply(c2).mod(nSquare);
    }

    public BigInteger getN() { return n; }
    public BigInteger getG() { return g; }
}
