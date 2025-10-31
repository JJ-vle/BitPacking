import java.util.*;
import java.io.*;
import java.util.function.Supplier;

public class Benchmark {

    private static final int REPEAT = 50;  // nombre de répétitions pour moyenne
    private static final int[] SIZES = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};
    private static final String[] TYPES = {"nooverlap", "overlap", "overflow"};
    private static final String[] CATEGORIES = {"uniform", "boltzmann", "edgecase32bit"};

    public static void main(String[] args) throws IOException {
        // créer le dossier de sortie
        File outDir = new File("data/out/");
        outDir.mkdirs();

        for (String category : CATEGORIES) {
            try (PrintWriter out = new PrintWriter(new FileWriter(new File(outDir, category + "_results.csv")))) {
                out.println("type,size,avgCompress(ms),avgDecompress(ms),avgGet(ns),ratio,tCrit(µs)");
                System.out.printf("=== Catégorie : %s ===%n", category);

                for (String type : TYPES) {
                    for (int n : SIZES) {
                        int[] input = readCSV("data/in/" + category + "_" + n + ".csv");

                        ICompressor compressor = BitPackingFactory.create(type);

                        // WARM-UP (stabiliser le JIT)
                        for (int i = 0; i < 5; i++) {
                            compressor.compress(input);
                            compressor.decompress();
                        }

                        // compression
                        double avgCompress = measureTimeMs(() -> compressor.compress(input));

                        int[] compressed = compressor.compress(input);

                        // décompression
                        double avgDecompress = measureTimeMs(() -> compressor.decompress());

                        // get(i)
                        Random rand = new Random(42);
                        int[] randomIdx = rand.ints(1000, 0, n).toArray();
                        double avgGet = measureTimeNs(() -> {
                            for (int idx : randomIdx) compressor.get(idx);
                        });

                        // ratio compression
                        double ratio = (double) compressed.length / input.length;

                        // latence critique
                        double tCrit = computeCriticalLatency(avgCompress, avgDecompress, ratio);

                        // affichage console
                        System.out.printf("%-10s %-10s %-10d %-15.3f %-15.3f %-15.1f %-15.3f%n",
                                category, type, n, avgCompress, avgDecompress, avgGet, tCrit);

                        // écriture CSV
                        out.printf(Locale.US, "%s,%s,%d,%.3f,%.3f,%.1f,%.6f,%.3f%n",
                                category, type, n, avgCompress, avgDecompress, avgGet, ratio, tCrit);
                    }
                }
            }
            System.out.println("Résultats sauvegardés pour la catégorie " + category + "\n");
        }
        System.out.println("Tous les benchmarks terminés !");
    }

    private static int[] readCSV(String path) throws IOException {
        List<Integer> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) list.add(Integer.parseInt(line.trim()));
            }
        }
        return list.stream().mapToInt(i -> i).toArray();
    }

    private static double measureTimeMs(Supplier<int[]> action) {
        long start = System.nanoTime();
        for (int r = 0; r < REPEAT; r++) action.get();
        long end = System.nanoTime();
        return (end - start) / 1e6 / REPEAT; // en ms
    }

    private static double measureTimeNs(Runnable action) {
        long start = System.nanoTime();
        action.run();
        long end = System.nanoTime();
        return (end - start) / 1000.0; // en ns
    }

    private static double computeCriticalLatency(double compMs, double decompMs, double ratio) {
        if (ratio >= 1.0) return Double.POSITIVE_INFINITY;
        double compPlusDecompMicro = (compMs + decompMs) * 1000.0; // en µs
        double n = 100_000.0; // base indicative
        return compPlusDecompMicro / (n * (1.0 - ratio));
    }
}
