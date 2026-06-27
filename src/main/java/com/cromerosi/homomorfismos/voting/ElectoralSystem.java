package com.cromerosi.homomorfismos.voting;

import com.cromerosi.homomorfismos.crypto.Paillier;

/**
 * Sistema de votación electrónica con cifrado homomórfico.
 */
public class ElectoralSystem {

    private final Paillier paillier;

    public ElectoralSystem(int keySize) {
        this.paillier = new Paillier(keySize);
    }

    public long encryptVote(int vote) {
        if (vote != 0 && vote != 1) {
            throw new IllegalArgumentException("Voto debe ser 0 o 1");
        }
        return paillier.encrypt(vote);
    }

    public long tallyEncryptedVotes(long[] encryptedVotes) {
        long encryptedSum = paillier.encrypt(0);
        if (encryptedVotes == null) {
            return encryptedSum;
        }

        for (long encryptedVote : encryptedVotes) {
            encryptedSum = paillier.homomorphicAdd(encryptedSum, encryptedVote);
        }
        return encryptedSum;
    }

    public long decryptResult(long encryptedSum) {
        return paillier.decrypt(encryptedSum);
    }
}
