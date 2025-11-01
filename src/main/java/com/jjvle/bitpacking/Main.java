package com.jjvle.bitpacking;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        List<Path> inputFiles = new ArrayList<>();

        if (args.length == 0) {
            // Mode automatique : tous les fichiers dans data/in/
            try {
                inputFiles = Files.walk(Paths.get("data/in"))
                                  .filter(Files::isRegularFile)
                                  .filter(p -> p.toString().endsWith(".csv"))
                                  .collect(Collectors.toList());
            } catch (IOException e) {
                System.err.println("Erreur en parcourant le dossier data/in : " + e.getMessage());
                System.exit(1);
            }
        } else {
            // Mode ciblé (chemin en arg)
            Path path = Paths.get(args[0]);
            if (!Files.exists(path)) {
                System.err.println("Fichier introuvable : " + path);
                System.exit(1);
            }
            inputFiles.add(path);
        }

        if (inputFiles.isEmpty()) {
            System.err.println("Aucun fichier trouvé à tester.");
            System.exit(0);
        }

        System.out.println("=== BitPacking Benchmark ===");
        System.out.println("Nombre de fichiers à traiter : " + inputFiles.size());

        for (Path file : inputFiles) {
            System.out.println("\nFichier : " + file.getFileName());

            try {
                int[] data = loadCSV(file.toString());
                runBenchmarks(data, file.getFileName().toString());
            } catch (Exception e) {
                System.err.println("Erreur sur " + file + " : " + e.getMessage());
            }
        }
    }

    private static void runBenchmarks(int[] data, String name) {
        List<String> types = Arrays.asList("nooverlap", "overlap", "overflow");

        for (String type : types) {
            ICompressor compressor = BitPackingFactory.create(type);

            long start = System.nanoTime();
            int[] compressed = compressor.compress(data);
            long compressTime = System.nanoTime() - start;

            start = System.nanoTime();
            int[] decompressed = compressor.decompress();
            long decompressTime = System.nanoTime() - start;

            boolean ok = Arrays.equals(data, decompressed);
            System.out.printf("   [%s] OK=%s | compress=%.2fms | decompress=%.2fms%n",
                    type, ok ? "YES" : "NO", compressTime / 1e6, decompressTime / 1e6);
        }
    }

    private static int[] loadCSV(String path) throws IOException {
        List<Integer> values = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                for (String token : line.split(",")) {
                    token = token.trim();
                    if (!token.isEmpty()) values.add(Integer.parseInt(token));
                }
            }
        }
        return values.stream().mapToInt(i -> i).toArray();
    }
}
