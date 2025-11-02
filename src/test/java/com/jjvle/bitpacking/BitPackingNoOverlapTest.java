package com.jjvle.bitpacking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BitPackingNoOverlapTest {

    @Test
    public void testSimpleCompressionDecompression() {
        int[] input = {1, 2, 3, 4, 5};
        BitPackingNoOverlap compressor = new BitPackingNoOverlap();

        int[] compressed = compressor.compress(input);
        int[] decompressed = new int[input.length];
        compressor.decompress(decompressed);

        assertArrayEquals(input, decompressed, "La décompression doit redonner les mêmes valeurs que l'entrée");
    }

    @Test
    public void testGetElement() {
        int[] input = {10, 20, 30, 40};
        BitPackingNoOverlap compressor = new BitPackingNoOverlap();
        compressor.compress(input);

        assertEquals(20, compressor.get(1), "get(1) doit renvoyer la deuxième valeur originale");
        assertEquals(40, compressor.get(3), "get(3) doit renvoyer la dernière valeur originale");
    }

    @Test
    public void testCompressionEfficiency() {
        int[] input = new int[1000];
        for (int i = 0; i < input.length; i++) input[i] = i % 64;

        BitPackingNoOverlap compressor = new BitPackingNoOverlap();
        int[] compressed = compressor.compress(input);

        assertTrue(compressed.length < input.length, "Le tableau compressé doit être plus court que l'entrée");
    }
}