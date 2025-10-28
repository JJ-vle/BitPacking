import java.util.*;

public class Benchmark {

    private static final int REPEAT = 50;  // nombre de répétitions pour moyenne
    private static final int[] SIZES = {1000, 10000, 100000, 1000000};

    public static void main(String[] args) {
        String[] types = {"nooverlap", "overlap", "overflow"};
        Random rand = new Random(42);

        System.out.printf("%-10s %-10s %-15s %-15s %-15s %-15s%n",
                "Type", "Size", "Comp (ms)", "Decomp (ms)", "Get (ns)", "LatencyCrit (µs)");

        for (String type : types) {
            for (int n : SIZES) {
                int[] input = rand.ints(n, 0, 5000).toArray();
                ICompressor compressor = BitPackingFactory.create(type);

                for (int i = 0; i < 5; i++) {
                    compressor.compress(input);
                    compressor.decompress();
                }

                // compression
                long start = System.nanoTime();
                for (int r = 0; r < REPEAT; r++) compressor.compress(input);
                long end = System.nanoTime();
                double avgCompress = (end - start) / 1e6 / REPEAT; // en ms

                int[] compressed = compressor.compress(input);

                // décompression
                start = System.nanoTime();
                for (int r = 0; r < REPEAT; r++) compressor.decompress();
                end = System.nanoTime();
                double avgDecompress = (end - start) / 1e6 / REPEAT; // en ms

                // get(i)
                int[] randomIdx = rand.ints(1000, 0, n).toArray();
                start = System.nanoTime();
                for (int idx : randomIdx) compressor.get(idx);
                end = System.nanoTime();
                double avgGet = (end - start) / 1000.0; // en ns

                // taille après compression
                int compressedInts = compressed.length;
                double ratio = (double) compressedInts / input.length;

                // calcul de la latence critique
                double tCrit = computeCriticalLatency(avgCompress, avgDecompress, ratio); // en µs

                // affichage
                System.out.printf("%-10s %-10d %-15.3f %-15.3f %-15.1f %-15.3f%n",
                        type, n, avgCompress, avgDecompress, avgGet, tCrit);
            }
        }
    }

    /**
     * Calcule le t (en µs) à partir duquel la compression devient rentable.
     * 
     * Soit :
     * Tnoncomp = n * t
     * Tcomp = comp + decomp + n * t * ratio
     * 
     * On cherche Tcomp = Tnoncomp → tCrit = (comp + decomp) / (n * (1 - ratio))
     */
    private static double computeCriticalLatency(double compMs, double decompMs, double ratio) {
        if (ratio >= 1.0) return Double.POSITIVE_INFINITY;
        double compPlusDecompMicro = (compMs + decompMs) * 1000.0; // en µs
        double n = 100000.0; // base indicative
        return compPlusDecompMicro / (n * (1.0 - ratio));
    }
}
