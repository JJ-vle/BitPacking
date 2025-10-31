package com.jjvle.bitpacking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BitPackingOverflowTest {

    @Test
    public void testCompressionDecompressionSimple() {
        int[] input = {1, 2, 3, 1024, 4, 5, 2048};
        BitPackingOverflow compressor = new BitPackingOverflow();

        int[] compressed = compressor.compress(input);
        assertNotNull(compressed, "Le tableau compressé ne doit pas être nul");

        int[] decompressed = new int[input.length];
        compressor.decompress(decompressed);

        assertArrayEquals(input, decompressed,
                "Les valeurs décompressées doivent correspondre à l'entrée");
    }

    @Test
    public void testGetElements() {
        int[] input = {1, 2, 3, 1024, 4, 5, 2048};
        BitPackingOverflow compressor = new BitPackingOverflow();
        compressor.compress(input);

        assertEquals(1, compressor.get(0), "Premier élément incorrect");
        assertEquals(1024, compressor.get(3), "Valeur overflow incorrecte");
        assertEquals(2048, compressor.get(6), "Dernière valeur overflow incorrecte");
    }

    @Test
    public void testOverflowZoneExists() {
        int[] input = {1, 2, 3, 1024, 4, 5, 2048};
        BitPackingOverflow compressor = new BitPackingOverflow();
        compressor.compress(input);

        // Vérifie qu'on a bien une zone overflow
        assertTrue(compressor.toString().contains("overflow="),
                "Le toString doit indiquer une zone overflow");
    }

    @Test
    public void testNoOverflowCase() {
        int[] input = {1, 2, 3, 4, 5, 6, 7};
        BitPackingOverflow compressor = new BitPackingOverflow();

        int[] compressed = compressor.compress(input);
        int[] decompressed = new int[input.length];
        compressor.decompress(decompressed);

        assertArrayEquals(input, decompressed,
                "Les petites valeurs ne doivent pas créer d'overflow");
    }

    @Test
    public void testRandomIntegrity() {
        int[] input = new int[200];
        for (int i = 0; i < input.length; i++) {
            // mélange petites et grandes valeurs
            input[i] = (i % 50 == 0) ? 5000 + i : i % 128;
        }

        BitPackingOverflow compressor = new BitPackingOverflow();
        compressor.compress(input);
        int[] output = new int[input.length];
        compressor.decompress(output);

        assertArrayEquals(input, output, "La compression/décompression doit être parfaitement réversible");
    }
}

