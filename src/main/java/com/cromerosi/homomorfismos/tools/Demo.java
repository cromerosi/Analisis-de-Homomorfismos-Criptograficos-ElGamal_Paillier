package com.cromerosi.homomorfismos.tools;

import com.cromerosi.homomorfismos.crypto.ElGamal;
import com.cromerosi.homomorfismos.crypto.Paillier;
import java.math.BigInteger;

public class Demo {
    public static void main(String[] args) {
        int keySize = 512;
        System.out.println("=== Demo de Criptosistemas Homomórficos (KeySize: " + keySize + ") ===");

        // Paillier
        Paillier paillier = new Paillier(keySize);
        BigInteger voto = BigInteger.valueOf(1);
        BigInteger c1 = paillier.encrypt(voto);
        System.out.println("\n[Paillier] Voto original: " + voto);
        System.out.println("[Paillier] Voto cifrado: " + c1.toString().substring(0, 30) + "...");
        System.out.println("[Paillier] Voto descifrado: " + paillier.decrypt(c1));

        // ElGamal
        ElGamal elGamal = new ElGamal(keySize);
        BigInteger[] e1 = elGamal.encrypt(voto);
        System.out.println("\n[ElGamal] Mensaje original: " + voto);
        System.out.println("[ElGamal] Cifrado [c1]: " + e1[0].toString().substring(0, 30) + "...");
        System.out.println("[ElGamal] Descifrado: " + elGamal.decrypt(e1));
    }
}
