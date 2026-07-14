package com.cromerosi.homomorfismos.math;

import java.math.BigInteger;

public class MathUtils {

    public static int gcd(int a, int b) {
        if (b == 0) return Math.abs(a);
        return gcd(b, a % b);
    }

    public static long modPow(long base, long exp, long mod) {
        if (mod == 1) return 0;
        BigInteger res = BigInteger.valueOf(base).modPow(BigInteger.valueOf(exp), BigInteger.valueOf(mod));
        return res.longValue();
    }

    public static long modInverse(long a, long mod) {
        BigInteger A = BigInteger.valueOf(a);
        BigInteger M = BigInteger.valueOf(mod);
        if (!A.gcd(M).equals(BigInteger.ONE)) {
            throw new ArithmeticException("Inverse does not exist");
        }
        return A.modInverse(M).longValue();
    }

    public static boolean isProbablyPrime(long n) {
        if (n < 2) return false;
        return BigInteger.valueOf(n).isProbablePrime(10);
    }
}
