package com.cromerosi.homomorfismos.voting;

import com.cromerosi.homomorfismos.crypto.Paillier;

public class ElectoralSystem {
    private final Paillier paillier;

    public ElectoralSystem(int keySize) {
        this.paillier = new Paillier(keySize);
    }

    public long encryptVote(int vote) {
        if (vote != 0 && vote != 1) {
            throw new IllegalArgumentException("El voto debe ser 0 o 1");
        }
        return paillier.encrypt(vote);
    }

    public long tallyEncryptedVotes(long[] encryptedVotes) {
        // Iniciamos la suma homomórfica cifrando un 0
        long encryptedSum = paillier.encrypt(0L);

        if (encryptedVotes == null || encryptedVotes.length == 0) {
            return encryptedSum;
        }

        for (long encryptedVote : encryptedVotes) {
            encryptedSum = paillier.homomorphicAdd(encryptedSum, encryptedVote);
        }
        return encryptedSum;
    }

    public int decryptResult(long encryptedSum) {
        return (int) paillier.decrypt(encryptedSum);
    }
}
