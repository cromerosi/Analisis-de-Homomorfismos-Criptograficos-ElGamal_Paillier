package com.cromerosi.homomorfismos.tools;

import com.cromerosi.homomorfismos.crypto.ElGamal;
import com.cromerosi.homomorfismos.crypto.Paillier;

/**
 * Demo mínimo para generar claves, cifrar y descifrar ejemplos.
 */
public class Demo {

    public static void main(String[] args) {
        int keySize = 512;
        System.out.println("Demo: generar claves y cifrar/descifrar (keySize=" + keySize + ")");

        Paillier paillier = new Paillier(keySize);
        System.out.println("Paillier public n: " + paillier.getPublicKey().getN());
        System.out.println("Paillier public g: " + paillier.getPublicKey().getG());

        long c1 = paillier.encrypt(1);
        System.out.println("Paillier cifrado(1): " + c1 + " -> descifrado: " + paillier.decrypt(c1));

        ElGamal elGamal = new ElGamal(keySize);
        System.out.println("ElGamal modulus: " + elGamal.getModulus());
        long[] e1 = elGamal.encrypt(1);
        System.out.println("ElGamal cifrado(1): [" + e1[0] + "," + e1[1] + "] -> descifrado: " + elGamal.decrypt(e1));
    }
}
