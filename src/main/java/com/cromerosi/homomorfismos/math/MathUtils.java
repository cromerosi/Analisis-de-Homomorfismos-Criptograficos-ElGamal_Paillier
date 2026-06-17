package com.cromerosi.homomorfismos.math;

import java.util.Random;

/**
 * Utilidades matemáticas para operaciones criptográficas.
 * Implementa funciones base sin dependencias externas.
 */
public final class MathUtils {

    private static final Random RANDOM = new Random();

    private MathUtils() {
        // Clase de utilidades, no instanciable
    }

    /**
     * Calcula el Máximo Común Divisor usando el algoritmo de Euclides.
     */
    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Exponenciación modular: calcula (base^exp) mod modulus de forma eficiente.
     */
    public static long modPow(long base, long exp, long modulus) {
        if (modulus == 1) return 0;
        long result = 1;
        base %= modulus;
        while (exp > 0) {
            if ((exp & 1) == 1) {
                result = (result * base) % modulus;
            }
            base = (base * base) % modulus;
            exp >>= 1;
        }
        return result;
    }

    /**
     * Calcula el inverso modular multiplicativo usando el Algoritmo Extendido de Euclides.
     * Lanza ArithmeticException si no existe el inverso.
     */
    public static long modInverse(long a, long m) {
        long[] result = extendedGcd(a, m);
        if (result[0] != 1) {
            throw new ArithmeticException("Inverso modular no existe para a=" + a + ", m=" + m);
        }
        return (result[1] % m + m) % m;
    }

    /**
     * Algoritmo Extendido de Euclides.
     * Retorna [gcd, x, y] tal que a*x + m*y = gcd.
     */
    private static long[] extendedGcd(long a, long m) {
        if (m == 0) {
            return new long[]{a, 1, 0};
        }
        long[] result = extendedGcd(m, a % m);
        long gcd = result[0];
        long x1 = result[1];
        long y1 = result[2];
        long x = y1;
        long y = x1 - (a / m) * y1;
        return new long[]{gcd, x, y};
    }

    /**
     * Test de primalidad probabilístico de Miller-Rabin.
     */
    public static boolean isProbablyPrime(long n) {
        if (n < 2) return false;
        if (n == 2 || n == 3) return true;
        if ((n & 1) == 0) return false;

        // Escribir n - 1 como 2^r * d
        long d = n - 1;
        int r = 0;
        while ((d & 1) == 0) {
            d >>= 1;
            r++;
        }

        // Prueba Miller-Rabin con k=5 iteraciones
        int k = 5;
        for (int i = 0; i < k; i++) {
            long a = 2 + RANDOM.nextLong() % (n - 3);
            if (!millerRabinTest(n, a, d, r)) {
                return false;
            }
        }
        return true;
    }

    private static boolean millerRabinTest(long n, long a, long d, int r) {
        long x = modPow(a, d, n);
        if (x == 1 || x == n - 1) {
            return true;
        }
        for (int i = 0; i < r - 1; i++) {
            x = (x * x) % n;
            if (x == n - 1) {
                return true;
            }
        }
        return false;
    }
}
