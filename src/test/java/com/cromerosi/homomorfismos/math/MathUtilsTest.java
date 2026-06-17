package com.cromerosi.homomorfismos.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utilidades Matemáticas")
class MathUtilsTest {

    @Nested
    @DisplayName("Pruebas de Máximo Común Divisor")
    class GcdTests {

        @Test
        @DisplayName("GCD de dos números positivos iguales")
        void testGcdEqualNumbers() {
            int result = MathUtils.gcd(12, 12);
            assertEquals(12, result);
        }

        @Test
        @DisplayName("GCD de dos números coprimos")
        void testGcdCoprime() {
            int result = MathUtils.gcd(13, 17);
            assertEquals(1, result);
        }

        @Test
        @DisplayName("GCD de números donde uno divide al otro")
        void testGcdDivisible() {
            int result = MathUtils.gcd(12, 4);
            assertEquals(4, result);
        }

        @Test
        @DisplayName("GCD con cero")
        void testGcdWithZero() {
            assertEquals(5, MathUtils.gcd(5, 0));
            assertEquals(7, MathUtils.gcd(0, 7));
        }
    }

    @Nested
    @DisplayName("Pruebas de Exponenciación Modular")
    class ModPowTests {

        @Test
        @DisplayName("Exponenciación modular básica: 2^3 mod 5 = 3")
        void testModPowBasic() {
            long result = MathUtils.modPow(2, 3, 5);
            assertEquals(3, result);
        }

        @Test
        @DisplayName("Exponenciación modular con resultado 1: 5^2 mod 8 = 1")
        void testModPowResultOne() {
            long result = MathUtils.modPow(5, 2, 8);
            assertEquals(1, result);
        }

        @Test
        @DisplayName("Exponenciación modular con exponente 0")
        void testModPowExponentZero() {
            long result = MathUtils.modPow(7, 0, 11);
            assertEquals(1, result);
        }

        @Test
        @DisplayName("Exponenciación modular con números grandes")
        void testModPowLargeNumbers() {
            long result = MathUtils.modPow(123456, 789, 1000000007);
            assertTrue(result >= 0 && result < 1000000007);
        }
    }

    @Nested
    @DisplayName("Pruebas de Modulo Inverso Multiplicativo")
    class ModInverseTests {

        @Test
        @DisplayName("Inverso modular de 3 mod 11 = 4")
        void testModInverseBasic() {
            long result = MathUtils.modInverse(3, 11);
            assertEquals((3 * result) % 11, 1);
        }

        @Test
        @DisplayName("Inverso modular no existe para números no coprimos")
        void testModInverseNonCoprime() {
            assertThrows(ArithmeticException.class, () -> {
                MathUtils.modInverse(4, 8);
            });
        }

        @Test
        @DisplayName("Inverso modular de 7 mod 26 = 15")
        void testModInverseVerified() {
            long result = MathUtils.modInverse(7, 26);
            assertEquals((7 * result) % 26, 1);
        }
    }

    @Nested
    @DisplayName("Pruebas de Primalidad")
    class PrimalityTests {

        @Test
        @DisplayName("Números primos pequeños")
        void testSmallPrimes() {
            assertTrue(MathUtils.isProbablyPrime(2));
            assertTrue(MathUtils.isProbablyPrime(3));
            assertTrue(MathUtils.isProbablyPrime(5));
            assertTrue(MathUtils.isProbablyPrime(7));
            assertTrue(MathUtils.isProbablyPrime(11));
        }

        @Test
        @DisplayName("Números no primos")
        void testNonPrimes() {
            assertFalse(MathUtils.isProbablyPrime(4));
            assertFalse(MathUtils.isProbablyPrime(6));
            assertFalse(MathUtils.isProbablyPrime(9));
            assertFalse(MathUtils.isProbablyPrime(10));
        }

        @Test
        @DisplayName("Números grandes probablemente primos")
        void testLargePrimes() {
            assertTrue(MathUtils.isProbablyPrime(1000000007));
        }
    }
}
