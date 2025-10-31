package com.jjvle.bitpacking;

public interface ICompressor {
    int[] compress(int[] array);
    int[] decompress();
    int[] decompress(int[] output); // décompression dans tableau déjà existant (évite allocations)
    int get(int index);
}
