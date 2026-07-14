package com.cromerosi.homomorfismos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.cromerosi.homomorfismos.crypto.ElGamal;
import com.cromerosi.homomorfismos.crypto.Paillier;
import com.sun.management.OperatingSystemMXBean;

/**
 * App: benchmark comparativo ElGamal vs Paillier aplicado a votación electrónica.
 *
 * Nota: este archivo implementa un runner que procesa archivos de votos en
 * `benchmarks_data/`. Se asume internamente (y se documenta) la API simple
 * requerida para esquemas criptográficos: operaciones con `BigInteger`.
 */
public final class App {

    // Config
    private static final Path DATA_ROOT = Paths.get("benchmarks_data");
    // Realistic numeric sizes for benchmark (stop at 1M)
    private static final int[] FILE_SIZES = new int[]{500, 5000, 50000, 500000, 1000000};
    private static final int FILES_PER_SIZE = 25;
    private static final int WARMUP_ITERATIONS = 10;
    private static final int IN_FLIGHT_MULTIPLIER = 4; // permitidos tasks in-flight = cores * multiplier
    private static final BigInteger VOTE_A = BigInteger.valueOf(1L);
    private static final BigInteger VOTE_B = new BigInteger("100000000"); // 10^8
    private static final BigInteger VOTE_C = new BigInteger("10000000000000000"); // 10^16

    private App() {}

    public static void main(String[] args) throws Exception {
        System.out.println("Benchmark ElGamal vs Paillier — iniciando...");

        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Cores detectados: " + cores);

        // Recolectar archivos por tamaño
        for (int size : FILE_SIZES) {
            System.out.println("Buscando archivos para tamaño: " + size);
        }

        // Preparar CSV parcial y shutdown hook para flush en interrupciones
        ensurePartialCsvHeader();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook: resultados parciales en 'resultados_parciales.csv'");
        }));

        List<ResultSummary> summaries = new ArrayList<>();

        // Warmup
        System.out.println("Calentamiento de la JVM: " + WARMUP_ITERATIONS + " iteraciones con tamaño " + FILE_SIZES[0] + "...");
        List<Path> warmFiles = collectFilesForSize(FILE_SIZES[0]);
        if (!warmFiles.isEmpty()) {
            Path warmFile = warmFiles.get(0);
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                runSingleBenchmark("Paillier", () -> new Paillier(512), warmFile, osBean, cores, true);
                runSingleBenchmark("ElGamal", () -> new ElGamal(512), warmFile, osBean, cores, true);
            }
        }

        // Ejecutar benchmark real
        for (int size : FILE_SIZES) {
            List<Path> files = collectFilesForSize(size);
            if (files.isEmpty()) {
                System.out.println("No se encontraron archivos para tamaño " + size + ", se omite.");
                continue;
            }

            // Asegurar hasta FILES_PER_SIZE o cuantos existan
            files = files.stream().limit(FILES_PER_SIZE).collect(Collectors.toList());

            // Paillier
            ResultSummary paillierSummary = runBenchmark("Paillier", () -> new Paillier(1024), files, osBean, cores, size);
            summaries.add(paillierSummary);

            // ElGamal
            ResultSummary elgamalSummary = runBenchmark("ElGamal", () -> new ElGamal(1024), files, osBean, cores, size);
            summaries.add(elgamalSummary);
        }

        // Mostrar y exportar resultados
        Path csvOut = Paths.get("resultados_benchmark.csv");
        try (BufferedWriter bw = Files.newBufferedWriter(csvOut, StandardCharsets.UTF_8)) {
            bw.write(ResultSummary.csvHeader());
            bw.newLine();
            for (ResultSummary s : summaries) {
                bw.write(s.toCsv());
                bw.newLine();
            }
        }

        // Mostrar resumen por tamaño y método, y calcular speedups
        System.out.println("\nResumen por tamaño y método:");
        System.out.printf(Locale.ROOT, "%10s %10s %10s %12s\n", "Size", "Method", "Files", "TotalMeanMs");
        for (ResultSummary s : summaries) {
            double mean = s.executions.stream().mapToDouble(e -> e.totalMs).average().orElse(0.0);
            System.out.printf(Locale.ROOT, "%10s %10s %10d %12.3f\n", s.sizeToken, s.method, s.executions.size(), mean);
        }

        // Speedup: Paillier vs ElGamal per size
        StringBuilder report = new StringBuilder();
        report.append("Informe ejecutivo de benchmark\n\n");
        for (int s : FILE_SIZES) {
            String size = String.valueOf(s);
            OptionalDouble pa = summaries.stream().filter(x -> x.sizeToken.equals(size) && x.method.equals("Paillier")).flatMapToDouble(x -> x.executions.stream().mapToDouble(e -> e.totalMs)).average();
            OptionalDouble el = summaries.stream().filter(x -> x.sizeToken.equals(size) && x.method.equals("ElGamal")).flatMapToDouble(x -> x.executions.stream().mapToDouble(e -> e.totalMs)).average();
            if (pa.isPresent() && el.isPresent()) {
                double speedup = el.getAsDouble() / pa.getAsDouble();
                report.append(String.format(Locale.ROOT, "Size %s: Paillier mean=%.3fms, ElGamal mean=%.3fms, Speedup(ElGamal/Paillier)=%.3f\n", size, pa.getAsDouble(), el.getAsDouble(), speedup));
            }
        }

        Path reportPath = Paths.get("informe_benchmark.txt");
        Files.write(reportPath, report.toString().getBytes(StandardCharsets.UTF_8));

        System.out.println("Benchmark completado. Resultados guardados en: " + csvOut.toAbsolutePath());
        System.out.println("Informe ejecutivo guardado en: " + reportPath.toAbsolutePath());
    }

    // --- Helpers and orchestrators -------------------------------------------------

    private static final Path PARTIAL_CSV = Paths.get("resultados_parciales.csv");
    private static final Object PARTIAL_CSV_LOCK = new Object();

    private static void ensurePartialCsvHeader() {
        try {
            synchronized (PARTIAL_CSV_LOCK) {
                if (!Files.exists(PARTIAL_CSV)) {
                    try (BufferedWriter bw = Files.newBufferedWriter(PARTIAL_CSV, StandardCharsets.UTF_8)) {
                        bw.write("Size,Method,File,LoadMs,EncryptMs,SumMs,DecryptMs,TotalMs,CPUTimeNs,CPUPercent,HeapBefore,HeapAfter,NumVotes,ThroughputVps,LatencyMsPerVote,CipherSizeBytes,Success,Error");
                        bw.newLine();
                    }
                }
            }
        } catch (IOException e) {
            // ignore header write failures
        }
    }

    private static void appendPartialCsv(int sizeToken, String method, ResultadoEjecucion r) {
        try {
            synchronized (PARTIAL_CSV_LOCK) {
                try (BufferedWriter bw = Files.newBufferedWriter(PARTIAL_CSV, StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND)) {
                    bw.write(String.format(Locale.ROOT, "%d,%s,%s,%.3f,%.3f,%.3f,%.3f,%.3f,%d,%.3f,%d,%d,%d,%.3f,%.6f,%d,%b,%s",
                            sizeToken,
                            method,
                            r.file.replace(',', '_'),
                            r.loadMs,
                            r.encryptMs,
                            r.sumMs,
                            r.decryptMs,
                            r.totalMs,
                            r.cpuTimeNs,
                            r.cpuPercent,
                            r.heapBeforeBytes,
                            r.heapAfterBytes,
                            r.numVotes,
                            r.throughputVps,
                            r.latencyMsPerVote,
                            r.ciphertextSizeBytes,
                            r.success,
                            r.error == null ? "" : r.error.replace(',', '_')
                    ));
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo escribir resultados parciales: " + e.getMessage());
        }
    }

    private static ResultSummary runBenchmark(String methodName, SupplierWithException<Object> schemeSupplier, List<Path> files, OperatingSystemMXBean osBean, int cores, int sizeToken) {
        List<ResultadoEjecucion> results = new ArrayList<>();
        int idx = 0;
        for (Path file : files) {
            idx++;
            System.out.printf(Locale.ROOT, "Probando %s: archivo %d/%d - %s\n", methodName, idx, files.size(), file.getFileName());
            ResultadoEjecucion r = runSingleBenchmark(methodName, schemeSupplier, file, osBean, cores, false);
            if (r != null) {
                results.add(r);
                appendPartialCsv(sizeToken, methodName, r);
            }
        }

        return ResultSummary.fromResults(methodName, String.valueOf(sizeToken), files.size(), results);
    }

    private static ResultadoEjecucion runSingleBenchmark(String methodName, SupplierWithException<Object> schemeSupplier, Path file, OperatingSystemMXBean osBean, int cores, boolean discardResult) {
        ResultadoEjecucion res = new ResultadoEjecucion();
        res.file = file.toString();
        long startTotal = System.nanoTime();
        try {
            // Temp files for streaming safety
            Path tmpConverted = Files.createTempFile("converted_", ".txt");
            Path tmpCipher = Files.createTempFile("cipher_", ".txt");

            // Phase A: carga y conversión
            long t0 = System.nanoTime();
            long lines = 0;
            try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8);
                 BufferedWriter bw = Files.newBufferedWriter(tmpConverted, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String s = line.trim();
                    if (s.isEmpty()) continue;
                    int v;
                    try { v = Integer.parseInt(s); } catch (NumberFormatException ex) { continue; }
                    BigInteger mapped = mapVoteToBigInteger(v);
                    bw.write(mapped.toString()); bw.newLine();
                    lines++;
                }
            }
            long t1 = System.nanoTime();
            res.loadMs = (t1 - t0) / 1_000_000.0;
            res.numVotes = lines;

            // Phase B: cifrado concurrente -> escribir ciphertexts en tmpCipher
            long encryptStartWall = System.nanoTime();
            long cpuStart = osBean.getProcessCpuTime();

            // Memory before encryption (try GC first)
            System.gc();
            long heapBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            res.heapBeforeBytes = heapBefore;

            int coresPool = Math.max(1, Runtime.getRuntime().availableProcessors());
            // Crear una única instancia del esquema para este archivo (evitar regenerar claves por voto)
            final Object schemeInstance = schemeSupplier.get();
            ExecutorService pool = Executors.newFixedThreadPool(coresPool);
            CompletionService<BigInteger> ecs = new ExecutorCompletionService<>(pool);
            Semaphore inFlight = new Semaphore(coresPool * IN_FLIGHT_MULTIPLIER);

            try (BufferedReader br = Files.newBufferedReader(tmpConverted, StandardCharsets.UTF_8);
                 BufferedWriter bw = Files.newBufferedWriter(tmpCipher, StandardCharsets.UTF_8)) {
                String line;
                long submitted = 0;
                while ((line = br.readLine()) != null) {
                    final BigInteger plain = new BigInteger(line.trim());
                    inFlight.acquire();
                    ecs.submit(() -> {
                        try {
                            if (schemeInstance instanceof Paillier) {
                                return ((Paillier) schemeInstance).encrypt(plain);
                            } else if (schemeInstance instanceof ElGamal) {
                                BigInteger[] pair = ((ElGamal) schemeInstance).encrypt(plain);
                                // Pack pair into single BigInteger: c1 << bits | c2
                                int bits = ((ElGamal) schemeInstance).getP().bitLength();
                                BigInteger packed = pair[0].shiftLeft(bits).or(pair[1]);
                                return packed;
                            } else {
                                throw new IllegalStateException("Unknown scheme");
                            }
                        } finally {
                            inFlight.release();
                        }
                    });
                    submitted++;
                }

                // Collect submitted results and write to tmpCipher
                for (long i = 0; i < submitted; i++) {
                    Future<BigInteger> f = ecs.take();
                    BigInteger cipher = f.get();
                    bw.write(cipher.toString()); bw.newLine();
                }
                pool.shutdown();
                pool.awaitTermination(1, TimeUnit.HOURS);
            } finally {
                if (!pool.isShutdown()) pool.shutdownNow();
            }

            long encryptEndWall = System.nanoTime();
            long cpuEnd = osBean.getProcessCpuTime();
            res.encryptMs = (encryptEndWall - encryptStartWall) / 1_000_000.0;
            res.cpuTimeNs = cpuEnd - cpuStart;
            // CPU percent for encryption phase across cores
            double encryptWallNs = (double) (encryptEndWall - encryptStartWall);
            if (encryptWallNs > 0) {
                res.cpuPercent = (res.cpuTimeNs / (encryptWallNs * cores)) * 100.0;
            } else res.cpuPercent = 0.0;

            // Phase C: suma homomórfica acumulativa
            long sumStart = System.nanoTime();
            BigInteger accumulated = null;
            // Use same scheme instance for operations
            Object schemeForOps = schemeInstance;
            try (BufferedReader br = Files.newBufferedReader(tmpCipher, StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    BigInteger c = new BigInteger(line.trim());
                    if (accumulated == null) accumulated = c;
                    else {
                        if (schemeForOps instanceof Paillier) {
                            accumulated = ((Paillier) schemeForOps).homomorphicAdd(accumulated, c);
                        } else if (schemeForOps instanceof ElGamal) {
                            // unpack
                            int bits = ((ElGamal) schemeForOps).getP().bitLength();
                            BigInteger c1 = c.shiftRight(bits);
                            BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
                            BigInteger c2 = c.and(mask);
                            BigInteger[] packed1 = new BigInteger[]{c1, c2};
                            // accumulated unpack
                            int bitsAcc = bits; // same
                            if (accumulated != null) {
                                BigInteger a1 = accumulated.shiftRight(bitsAcc);
                                BigInteger a2 = accumulated.and(mask);
                                BigInteger nc1 = a1.multiply(c1).mod(((ElGamal) schemeForOps).getP());
                                BigInteger nc2 = a2.multiply(c2).mod(((ElGamal) schemeForOps).getP());
                                accumulated = nc1.shiftLeft(bitsAcc).or(nc2);
                            }
                        }
                    }
                }
            }
            long sumEnd = System.nanoTime();
            res.sumMs = (sumEnd - sumStart) / 1_000_000.0;

            // Phase D: descifrado
            long decStart = System.nanoTime();
            BigInteger finalPlain = null;
            if (accumulated != null) {
                if (schemeForOps instanceof Paillier) {
                    finalPlain = ((Paillier) schemeForOps).decrypt(accumulated);
                } else if (schemeForOps instanceof ElGamal) {
                    int bits = ((ElGamal) schemeForOps).getP().bitLength();
                    BigInteger a1 = accumulated.shiftRight(bits);
                    BigInteger mask = BigInteger.ONE.shiftLeft(bits).subtract(BigInteger.ONE);
                    BigInteger a2 = accumulated.and(mask);
                    BigInteger[] pair = new BigInteger[]{a1, a2};
                    finalPlain = ((ElGamal) schemeForOps).decrypt(pair);
                }
            } else {
                finalPlain = BigInteger.ZERO;
            }
            long decEnd = System.nanoTime();
            res.decryptMs = (decEnd - decStart) / 1_000_000.0;

            long endTotal = System.nanoTime();
            res.totalMs = (endTotal - startTotal) / 1_000_000.0;
            // throughput and latency
            if (res.numVotes > 0) {
                res.throughputVps = (res.numVotes) / (res.totalMs / 1000.0);
                res.latencyMsPerVote = res.totalMs / res.numVotes;
            }

            // Memory metrics
            System.gc();
            long heapUsedAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            res.heapAfterBytes = heapUsedAfter;

            // Ciphertext size (first line of tmpCipher)
            try (BufferedReader br = Files.newBufferedReader(tmpCipher, StandardCharsets.UTF_8)) {
                String first = br.readLine();
                if (first != null) res.ciphertextSizeBytes = new BigInteger(first).toByteArray().length;
            }

            res.success = true;
            res.plainResult = finalPlain == null ? BigInteger.ZERO : finalPlain;

            // cleanup
            try { Files.deleteIfExists(tmpConverted); } catch (Exception ignored) {}
            try { Files.deleteIfExists(tmpCipher); } catch (Exception ignored) {}

            if (!discardResult) System.out.printf(Locale.ROOT, "-> OK: carga %.2fms cifrado %.2fms suma %.2fms desc %.2fms total %.2fms\n", res.loadMs, res.encryptMs, res.sumMs, res.decryptMs, res.totalMs);
            return res;

        } catch (Throwable t) {
            res.success = false;
            res.error = t.getMessage();
            System.err.println("Error procesando " + file + ": " + t.getMessage());
            return res;
        }
    }

    private static BigInteger mapVoteToBigInteger(int v) {
        switch (v) {
            case 0: return VOTE_A;
            case 1: return VOTE_B;
            case 2: return VOTE_C;
            default: return BigInteger.ZERO; // ignorar
        }
    }

    private static List<Path> collectFilesForSize(int sizeToken) {
        List<Path> found = new ArrayList<>();
        try {
            // Prefer folder benchmarks_data/<sizeToken>/
            Path dir = DATA_ROOT.resolve(String.valueOf(sizeToken));
            if (Files.isDirectory(dir)) {
                Files.list(dir).filter(p -> p.getFileName().toString().endsWith(".txt")).forEach(found::add);
            } else if (Files.isDirectory(DATA_ROOT)) {
                // fallback: search files containing the token
                Files.list(DATA_ROOT).filter(p -> p.getFileName().toString().contains("votos_" + sizeToken + "_") && p.getFileName().toString().endsWith(".txt")).forEach(found::add);
            }
        } catch (IOException e) {
            // ignore
        }
        Collections.sort(found, Comparator.comparing(Path::toString));
        return found;
    }

    // --- Data classes --------------------------------------------------------------

    private static class ResultadoEjecucion {
        String file;
        double loadMs;
        double encryptMs;
        double sumMs;
        double decryptMs;
        double totalMs;
        long cpuTimeNs;
        long heapAfterBytes;
        long heapBeforeBytes;
        double cpuPercent;
        long numVotes;
        double throughputVps;
        double latencyMsPerVote;
        int ciphertextSizeBytes;
        boolean success = false;
        String error;
        BigInteger plainResult;
    }

    private static class ResultSummary {
        String method;
        String sizeToken;
        int files;
        List<ResultadoEjecucion> executions;

        static ResultSummary fromResults(String method, String sizeToken, int files, List<ResultadoEjecucion> executions) {
            ResultSummary s = new ResultSummary();
            s.method = method; s.sizeToken = sizeToken; s.files = files; s.executions = executions;
            return s;
        }

        static String csvHeader() {
            return "Size,Metodo,Files,Count,LoadMeanMs,EncryptMeanMs,SumMeanMs,DecryptMeanMs,TotalMeanMs,TotalStdMs,TotalMedianMs,P25Ms,P75Ms,P95Ms,P99Ms,ThroughputMeanVps,LatencyMeanMsPerVote,CPUPercentMean,HeapBeforeMeanBytes,HeapAfterMeanBytes,CipherSizeMeanBytes";
        }

        String toCsv() {
            List<Double> totals = executions.stream().map(e -> e.totalMs).collect(Collectors.toList());
            Stats totalStats = Stats.of(totals);

            double loadMean = Stats.of(executions.stream().map(e -> e.loadMs).collect(Collectors.toList())).mean;
            double encMean = Stats.of(executions.stream().map(e -> e.encryptMs).collect(Collectors.toList())).mean;
            double sumMean = Stats.of(executions.stream().map(e -> e.sumMs).collect(Collectors.toList())).mean;
            double decMean = Stats.of(executions.stream().map(e -> e.decryptMs).collect(Collectors.toList())).mean;
            double throughputMean = Stats.of(executions.stream().map(e -> e.throughputVps).collect(Collectors.toList())).mean;
            double latencyMean = Stats.of(executions.stream().map(e -> e.latencyMsPerVote).collect(Collectors.toList())).mean;
            double cpuPercentMean = Stats.of(executions.stream().map(e -> e.cpuPercent).collect(Collectors.toList())).mean;
            double heapBeforeMean = Stats.of(executions.stream().map(e -> (double)e.heapBeforeBytes).collect(Collectors.toList())).mean;
            double heapAfterMean = Stats.of(executions.stream().map(e -> (double)e.heapAfterBytes).collect(Collectors.toList())).mean;
            double cipherSizeMean = Stats.of(executions.stream().map(e -> (double)e.ciphertextSizeBytes).collect(Collectors.toList())).mean;

            return String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%.0f,%.0f,%.1f,%.1f", sizeToken, method, files, executions.size(), loadMean, encMean, sumMean, decMean, totalStats.mean, totalStats.std, totalStats.median, totalStats.p25, totalStats.p75, totalStats.p95, totalStats.p99, throughputMean, latencyMean, cpuPercentMean, heapBeforeMean, heapAfterMean, cipherSizeMean, cipherSizeMean);
        }
    }

    private static class Stats {
        final double mean; final double std; final double min; final double max; final double median; final double p25; final double p75; final double p95; final double p99;
        private Stats(double mean, double std, double min, double max, double median, double p25, double p75, double p95, double p99) {
            this.mean=mean; this.std=std; this.min=min; this.max=max; this.median=median; this.p25=p25; this.p75=p75; this.p95=p95; this.p99=p99;
        }

        static Stats of(List<Double> values) {
            if (values == null || values.isEmpty()) return new Stats(0,0,0,0,0,0,0,0,0);
            List<Double> v = new ArrayList<>(values);
            Collections.sort(v);
            double sum=0; for (double x:v) sum+=x; double mean=sum/v.size();
            double var=0; for (double x:v) var+=(x-mean)*(x-mean); var /= v.size(); double std=Math.sqrt(var);
            double min=v.get(0), max=v.get(v.size()-1);
            double median = percentile(v,50);
            double p25 = percentile(v,25); double p75 = percentile(v,75); double p95 = percentile(v,95); double p99 = percentile(v,99);
            return new Stats(mean,std,min,max,median,p25,p75,p95,p99);
        }

        private static double percentile(List<Double> sorted, double p) {
            if (sorted.isEmpty()) return 0;
            double idx = p/100.0*(sorted.size()-1);
            int lo = (int)Math.floor(idx); int hi = (int)Math.ceil(idx);
            if (lo==hi) return sorted.get(lo);
            double a = sorted.get(lo); double b = sorted.get(hi); return a + (b-a)*(idx-lo);
        }
    }

    // Simple functional supplier that can throw
    @FunctionalInterface
    private interface SupplierWithException<T> { T get() throws Exception; }
}