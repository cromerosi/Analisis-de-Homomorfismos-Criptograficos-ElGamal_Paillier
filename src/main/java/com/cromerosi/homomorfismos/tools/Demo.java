package com.cromerosi.homomorfismos.tools;

import com.cromerosi.homomorfismos.crypto.ElGamal;
import com.cromerosi.homomorfismos.crypto.Paillier;
import java.math.BigInteger;

public class Demo {
    public static void main(String[] args) {
        int keySize = 2048; //1024 o 2048
        System.out.println("=== BENCHMARK DE CRIPTOSISTEMAS HOMOMÓRFICOS (KeySize: " + keySize + ") ===");

        BigInteger voto = BigInteger.valueOf(1);

        // ==========================================
        // PRUEBA DE PAILLIER
        // ==========================================
        System.out.println("\n--- Ejecutando Paillier ---");
        long inicioPaillier = System.nanoTime();
        
        Paillier paillier = new Paillier(keySize);
        BigInteger c1 = paillier.encrypt(voto);
        BigInteger descifradoPaillier = paillier.decrypt(c1);
        
        long finPaillier = System.nanoTime();
        double tiempoPaillier = (finPaillier - inicioPaillier) / 1e6;

        System.out.println("[Paillier] Voto original: " + voto);
        System.out.println("[Paillier] Voto cifrado: " + c1.toString().substring(0, 30) + "...");
        System.out.println("[Paillier] Voto descifrado: " + descifradoPaillier);
        System.out.println("[Paillier] TIEMPO TOTAL: " + tiempoPaillier + " ms");

        // ==========================================
        // PRUEBA DE ELGAMAL
        // ==========================================
        System.out.println("\n--- Ejecutando ElGamal ---");
        long inicioElGamal = System.nanoTime();
        
        ElGamal elGamal = new ElGamal(keySize);
        BigInteger[] e1 = elGamal.encrypt(voto);
        BigInteger descifradoElGamal = elGamal.decrypt(e1);
        
        long finElGamal = System.nanoTime();
        double tiempoElGamal = (finElGamal - inicioElGamal) / 1e6;

        System.out.println("[ElGamal] Mensaje original: " + voto);
        System.out.println("[ElGamal] Cifrado [c1]: " + e1[0].toString().substring(0, 30) + "...");
        System.out.println("[ElGamal] Descifrado: " + descifradoElGamal);
        System.out.println("[ElGamal] TIEMPO TOTAL: " + tiempoElGamal + " ms");
    }
}
