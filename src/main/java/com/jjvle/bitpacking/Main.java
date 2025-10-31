package com.jjvle.bitpacking;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] data = {1, 2, 3, 10, 15, 7, 8, 31};

        ICompressor compressor = BitPackingFactory.create("overflow");

        int[] compressed = compressor.compress(data);
        int[] decompressed = compressor.decompress();

        System.out.println("Original:      " + Arrays.toString(data));
        System.out.println("Decompressed:  " + Arrays.toString(decompressed));
        System.out.println("Element [4] =  " + compressor.get(4));
    }
}
