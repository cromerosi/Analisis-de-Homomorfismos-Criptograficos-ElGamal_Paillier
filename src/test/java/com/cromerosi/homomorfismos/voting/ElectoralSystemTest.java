package com.cromerosi.homomorfismos.voting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas del Sistema de Votación Electrónica Homomórfica")
class ElectoralSystemTest {

    private ElectoralSystem electoralSystem;

    @BeforeEach
    void setUp() {
        electoralSystem = new ElectoralSystem(512);
    }

    @Nested
    @DisplayName("Cifrado de Votos")
    class VoteEncryptionTests {

        @Test
        @DisplayName("Cifrar voto válido (1)")
        void testEncryptValidVoteYes() {
            long encryptedVote = electoralSystem.encryptVote(1);
            assertNotEquals(1, encryptedVote);
        }

        @Test
        @DisplayName("Cifrar voto válido (0)")
        void testEncryptValidVoteNo() {
            long encryptedVote = electoralSystem.encryptVote(0);
            assertNotEquals(0, encryptedVote);
        }

        @Test
        @DisplayName("No permitir votos inválidos")
        void testRejectInvalidVote() {
            assertThrows(IllegalArgumentException.class, () -> {
                electoralSystem.encryptVote(2);
            });
            assertThrows(IllegalArgumentException.class, () -> {
                electoralSystem.encryptVote(-1);
            });
        }
    }

    @Nested
    @DisplayName("Recuento Homomórfico de Votos")
    class VoteCountingTests {

        @Test
        @DisplayName("Sumar votos cifrados sin revelar individuales")
        void testHomomorphicVoteCount() {
            // 3 votos: Sí, No, Sí
            long vote1 = electoralSystem.encryptVote(1);
            long vote2 = electoralSystem.encryptVote(0);
            long vote3 = electoralSystem.encryptVote(1);

            long[] encryptedVotes = {vote1, vote2, vote3};
            long encryptedSum = electoralSystem.tallyEncryptedVotes(encryptedVotes);
            long result = electoralSystem.decryptResult(encryptedSum);

            // Esperado: 2 (dos votos afirmativos)
            assertEquals(2, result);
        }

        @Test
        @DisplayName("Recuento de cero votos")
        void testEmptyBallot() {
            long[] emptyVotes = {};
            long encryptedSum = electoralSystem.tallyEncryptedVotes(emptyVotes);
            long result = electoralSystem.decryptResult(encryptedSum);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Recuento de un único voto")
        void testSingleVote() {
            long vote = electoralSystem.encryptVote(1);
            long[] votes = {vote};
            long encryptedSum = electoralSystem.tallyEncryptedVotes(votes);
            long result = electoralSystem.decryptResult(encryptedSum);

            assertEquals(1, result);
        }
    }

    @Nested
    @DisplayName("Privacidad del Votante")
    class VoterPrivacyTests {

        @Test
        @DisplayName("Votos cifrados no revelan el contenido")
        void testVoteConfidentiality() {
            long yesVote = electoralSystem.encryptVote(1);
            long noVote = electoralSystem.encryptVote(0);

            // Los criptogramas deben ser distintos
            assertNotEquals(yesVote, noVote);
        }

        @Test
        @DisplayName("El mismo voto genera criptogramas distintos (randomización)")
        void testVoteRandomization() {
            long vote1 = electoralSystem.encryptVote(1);
            long vote2 = electoralSystem.encryptVote(1);

            // Aunque ambos sean "1", los criptogramas deben diferir
            assertNotEquals(vote1, vote2);
        }
    }

    @Nested
    @DisplayName("Integridad de Resultados")
    class ResultIntegrityTests {

        @Test
        @DisplayName("Resultado correcto con muchos votos")
        void testLargeScaleVoting() {
            int totalVotes = 100;
            int affirmativeVotes = 63;
            int negativeVotes = 37;

            long[] votes = new long[totalVotes];
            for (int i = 0; i < affirmativeVotes; i++) {
                votes[i] = electoralSystem.encryptVote(1);
            }
            for (int i = affirmativeVotes; i < totalVotes; i++) {
                votes[i] = electoralSystem.encryptVote(0);
            }

            long encryptedSum = electoralSystem.tallyEncryptedVotes(votes);
            long result = electoralSystem.decryptResult(encryptedSum);

            assertEquals(affirmativeVotes, result);
        }

        @Test
        @DisplayName("Verificación sin descifrar individuos")
        void testVerificationWithoutRevealingIndividualVotes() {
            long vote = electoralSystem.encryptVote(1);
            long[] votes = {vote};

            // El recuento homomórfico debe funcionar sin descifrar el voto individual
            long encryptedSum = electoralSystem.tallyEncryptedVotes(votes);
            long result = electoralSystem.decryptResult(encryptedSum);

            assertEquals(1, result);
            // El voto individual nunca fue descifrado
        }
    }
}
