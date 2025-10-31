package com.jjvle.bitpacking;

public class BitPackingNoOverlap implements ICompressor {
    private int[] compressedData;
    private int bitWidth;
    private int originalLength;

    private int neededBits(int maxValue) {
        int bits = 32 - Integer.numberOfLeadingZeros(maxValue);
        return Math.max(bits, 1);
    }

    /**
     * Compression (version sans chevauchement)
     */
    @Override
    public int[] compress(int[] array) {
        this.originalLength = array.length;

        // Trouver valeur max pour déterminer taille en bits
        int max = 0;
        for (int v : array) {
            if (v > max) max = v;
        }
        this.bitWidth = neededBits(max);

        // Chaque entier compressé tient entièrement dans un "slot"
        // On met plusieurs slots par int 32 bits si possible
        int slotsPerInt = 32 / bitWidth;
        int compressedLength = (int) Math.ceil((double) array.length / slotsPerInt);
        compressedData = new int[compressedLength];

        for (int i = 0; i < array.length; i++) {
            int value = array[i];
            int blockIndex = i / slotsPerInt; // index du int qui contient la valeur
            int offset = (i % slotsPerInt) * bitWidth; // décalage en bits

            compressedData[blockIndex] |= (value << offset);
        }

        return compressedData;
    }

    /**
     * Décompression
     */
    @Override
    public int[] decompress() {
        int[] result = new int[originalLength];
        return decompress(result);
    }

    @Override
    public int[] decompress(int[] output) {
        int slotsPerInt = 32 / bitWidth;

        for (int i = 0; i < originalLength; i++) {
            int blockIndex = i / slotsPerInt;
            int offset = (i % slotsPerInt) * bitWidth;

            int mask = (1 << bitWidth) - 1;
            output[i] = (compressedData[blockIndex] >> offset) & mask;
        }

        return output;
    }

    /**
     * Accès direct à l’élément i sans tout décompresser
     */
    @Override
    public int get(int i) {
        int slotsPerInt = 32 / bitWidth;
        int blockIndex = i / slotsPerInt;
        int offset = (i % slotsPerInt) * bitWidth;

        int mask = (1 << bitWidth) - 1;
        return (compressedData[blockIndex] >> offset) & mask;
    }

}
