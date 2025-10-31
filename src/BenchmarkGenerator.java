import java.io.*;
import java.util.*;

public class BenchmarkGenerator {
    private static final Random rand = new Random(42);

    public static int[] generateUniform(int n, int max) {
        return rand.ints(n, 0, max).toArray();
    }

    public static int[] generateBoltzmann(int n, double lambda, int max) {
        int[] data = new int[n];
        for (int i = 0; i < n; i++) {
            double r = rand.nextDouble();
            int value = (int)(-Math.log(1 - r) / lambda);
            data[i] = Math.min(value, max);
        }
        return data;
    }

    public static int[] generateEdgeCase32bit(int n) {
        int[] data = new int[n];
        for (int i = 0; i < n; i++) data[i] = (i % 2 == 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        return data;
    }

    public static void saveToCSV(String name, int[] array) throws IOException {
        File file = new File("data/in/" + name + ".csv");
        file.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(file)) {
            for (int val : array) pw.println(val);
        }
    }


    /* 
    // generations de benchmarks
    public static void main(String[] args) throws IOException {
        int[] SIZES = {1_000, 10_000, 100_000, 1_000_000, 10_000_000};

        for (int n : SIZES) {
            // Uniform
            int[] uniform = generateUniform(n, 5000);
            saveToCSV("uniform_" + n, uniform);

            // Boltzmann
            int[] boltz = generateBoltzmann(n, 1e-4, 65536);
            saveToCSV("boltzmann_" + n, boltz);

            // Edge case (32-bit)
            int[] edge = generateEdgeCase32bit(n);
            saveToCSV("edgecase32bit_" + n, edge);
        }

        System.out.println("fini");
    }
        */

    
}
