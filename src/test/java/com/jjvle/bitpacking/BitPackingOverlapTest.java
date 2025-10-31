package com.jjvle.bitpacking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BitPackingOverlapTest {

    @Test
    public void testCompressionDecompressionOverlap() {
        int[] input = {15, 1023, 7, 500, 31, 0};
        BitPackingOverlap compressor = new BitPackingOverlap();

        int[] compressed = compressor.compress(input);
        int[] decompressed = new int[input.length];
        compressor.decompress(decompressed);

        assertArrayEquals(input, decompressed, "La décompression doit redonner les mêmes valeurs que l'entrée (avec chevauchement)");
    }

    @Test
    public void testGetElementOverlap() {
        int[] input = {12, 34, 56, 78, 90};
        BitPackingOverlap compressor = new BitPackingOverlap();
        compressor.compress(input);

        assertEquals(12, compressor.get(0));
        assertEquals(78, compressor.get(3));
        assertEquals(90, compressor.get(4));
    }

    @Test
    public void testCompressionOverlapEfficiency() {
        int[] input = new int[200];
        for (int i = 0; i < input.length; i++) input[i] = i % 128;

        BitPackingOverlap compressor = new BitPackingOverlap();
        int[] compressed = compressor.compress(input);

        assertTrue(compressed.length < input.length, "Le tableau compressé doit être plus court que l'entrée (chevauchement)");
    }
}
