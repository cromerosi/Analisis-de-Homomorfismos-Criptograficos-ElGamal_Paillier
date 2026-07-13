package com.cromerosi.homomorfismos.voting;

import com.cromerosi.homomorfismos.crypto.Paillier;
import java.math.BigInteger;
import java.util.List;

public class ElectoralSystem {
    private final Paillier paillier;

    public ElectoralSystem(int keySize) {
        this.paillier = new Paillier(keySize);
    }

    public BigInteger encryptVote(int vote) {
        if (vote != 0 && vote != 1) {
            throw new IllegalArgumentException("El voto debe ser 0 o 1");
        }
        return paillier.encrypt(BigInteger.valueOf(vote));
    }

    public BigInteger tallyEncryptedVotes(List<BigInteger> encryptedVotes) {
        // Iniciamos la suma homomórfica cifrando un 0
        BigInteger encryptedSum = paillier.encrypt(BigInteger.ZERO);
        
        if (encryptedVotes == null || encryptedVotes.isEmpty()) {
            return encryptedSum;
        }

        // Computamos el total operando exclusivamente sobre datos cifrados
        for (BigInteger encryptedVote : encryptedVotes) {
            encryptedSum = paillier.homomorphicAdd(encryptedSum, encryptedVote);
        }
        return encryptedSum;
    }

    public int decryptResult(BigInteger encryptedSum) {
        return paillier.decrypt(encryptedSum).intValue();
    }
}
