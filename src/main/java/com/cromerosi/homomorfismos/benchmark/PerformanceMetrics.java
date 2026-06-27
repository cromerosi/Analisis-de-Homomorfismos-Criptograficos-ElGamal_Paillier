package com.cromerosi.homomorfismos.benchmark;

/**
 * Registro de métricas de rendimiento para comparación de esquemas.
 */
public class PerformanceMetrics {

    private long elGamalEncryptionTime;
    private long paillierEncryptionTime;
    private long elGamalDecryptionTime;
    private long paillierDecryptionTime;
    private long elGamalHomomorphicOpTime;
    private long paillierHomomorphicOpTime;

    public PerformanceMetrics recordElGamalEncryptionTime(long time) {
        this.elGamalEncryptionTime = time;
        return this;
    }

    public PerformanceMetrics recordPaillierEncryptionTime(long time) {
        this.paillierEncryptionTime = time;
        return this;
    }

    public PerformanceMetrics recordElGamalDecryptionTime(long time) {
        this.elGamalDecryptionTime = time;
        return this;
    }

    public PerformanceMetrics recordPaillierDecryptionTime(long time) {
        this.paillierDecryptionTime = time;
        return this;
    }

    public PerformanceMetrics recordElGamalHomomorphicOpTime(long time) {
        this.elGamalHomomorphicOpTime = time;
        return this;
    }

    public PerformanceMetrics recordPaillierHomomorphicOpTime(long time) {
        this.paillierHomomorphicOpTime = time;
        return this;
    }

    public long compareEncryptionSpeed(long elGamalTime, long paillierTime) {
        // Retorna diferencia absoluta para comparación robusta en tests.
        return Math.abs(elGamalTime - paillierTime);
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{" +
                "elGamalEncryptionTime=" + elGamalEncryptionTime +
                ", paillierEncryptionTime=" + paillierEncryptionTime +
                ", elGamalDecryptionTime=" + elGamalDecryptionTime +
                ", paillierDecryptionTime=" + paillierDecryptionTime +
                ", elGamalHomomorphicOpTime=" + elGamalHomomorphicOpTime +
                ", paillierHomomorphicOpTime=" + paillierHomomorphicOpTime +
                '}';
    }
}
