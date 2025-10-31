package com.jjvle.bitpacking;

import java.util.*;

/**
 * BitPackingOverflow implements integer compression with overflow zones.
 * 
 * Idea:
 * - Most integers are encoded with k' bits (baseBits)
 * - Large integers that need more bits go to an overflow area
 * - In the main zone, each value is prefixed by one indicator bit:
 *      - 0 = normal value (stored on baseBits)
 *      - 1 = pointer to overflow area (index stored on overflowBits)
 */
public class BitPackingOverflow implements ICompressor {

    private int[] compressedData;          // compressed main area
    private int[] overflowData;            // overflow area (true large values)
    private int baseBits;                  // number of bits used for small values
    private int overflowBits;              // bits to encode an overflow index
    private int totalBitsPerEntry;         // = 1 + max(baseBits, overflowBits)
    private int originalLength;                         // number of original integers
    private int[] original;                // optional reference (for tests)

    @Override
    public int[] compress(int[] input) {
        this.originalLength = input.length;
        this.original = input;

        // nb bits pour chaque val
        int max = Arrays.stream(input).max().orElse(0);
        int minBits = bitWidth(max);

        // bon basebit
        int[] sorted = input.clone();
        Arrays.sort(sorted);
        int threshold = sorted[(int)(0.75 * sorted.length)];
        this.baseBits = bitWidth(threshold);

        // identifie overflow val12
        List<Integer> overflowList = new ArrayList<>();
        for (int v : input) {
            if (bitWidth(v) > baseBits) overflowList.add(v);
        }

        int overflowCount = overflowList.size();
        this.overflowBits = overflowCount == 0 ? 1 : bitWidth(overflowCount - 1);
        this.totalBitsPerEntry = 1 + Math.max(baseBits, overflowBits);

        // alloue bit
        long totalBits = (long) totalBitsPerEntry * originalLength;
        int numInts = (int) Math.ceil(totalBits / 32.0);
        compressedData = new int[numInts];
        overflowData = overflowList.stream().mapToInt(Integer::intValue).toArray();

        // pack vals
        long bitPos = 0;
        int overflowIndex = 0;
        for (int value : input) {
            boolean isOverflow = bitWidth(value) > baseBits;
            int flag = isOverflow ? 1 : 0;
            int data = isOverflow ? overflowIndex++ : value;
            int bits = isOverflow ? overflowBits : baseBits;
            packBits(flag, 1, bitPos);
            bitPos += 1;
            packBits(data, bits, bitPos);
            bitPos += bits;
        }

        return compressedData;
    }

    @Override
    public int[] decompress() {
        int[] result = new int[originalLength];
        return decompress(result);
    }

    @Override
    public int[] decompress(int[] output) {
        long bitPos = 0;
        int overflowIndex;
        for (int i = 0; i < originalLength; i++) {
            int flag = (int) unpackBits(bitPos, 1);
            bitPos += 1;
            if (flag == 0) {
                output[i] = (int) unpackBits(bitPos, baseBits);
                bitPos += baseBits;
            } else {
                overflowIndex = (int) unpackBits(bitPos, overflowBits);
                bitPos += overflowBits;
                output[i] = overflowData[overflowIndex];
            }
        }
        return output;
    }
/* 
    @Override
    public int get(int i) {
        long bitPos = (long) i * totalBitsPerEntry;
        int flag = (int) unpackBits(bitPos, 1);
        bitPos += 1;
        if (flag == 0) {
            return (int) unpackBits(bitPos, baseBits);
        } else {
            int overflowIndex = (int) unpackBits(bitPos, overflowBits);
            return overflowData[overflowIndex];
        }
    }
*/

    @Override
    public int get(int i) {
        long bitPos = 0;
        for (int k = 0; k < i; k++) {
            int flag = (int) unpackBits(bitPos, 1);
            bitPos += 1;
            bitPos += (flag == 0 ? baseBits : overflowBits);
        }

        int flag = (int) unpackBits(bitPos, 1);
        bitPos += 1;

        if (flag == 0) {
            return (int) unpackBits(bitPos, baseBits);
        } else {
            int overflowIndex = (int) unpackBits(bitPos, overflowBits);
            if (overflowIndex < 0 || overflowIndex >= overflowData.length) {
                throw new IllegalStateException(String.format(
                    "Invalid overflow index %d (max %d) at i=%d", 
                    overflowIndex, overflowData.length - 1, i
                ));
            }
            return overflowData[overflowIndex];
        }
    }


    /** Packs 'value' into compressedData at bit position bitPos using bitCount bits */
    private void packBits(int value, int bitCount, long bitPos) {
        int index = (int) (bitPos / 32);
        int offset = (int) (bitPos % 32);
        long mask = ((1L << bitCount) - 1);
        long val = (value & mask) << offset;

        long existing = Integer.toUnsignedLong(compressedData[index]) | val;
        compressedData[index] = (int) existing;

        int remainingBits = 32 - offset;
        if (bitCount > remainingBits) {
            int nextBits = bitCount - remainingBits;
            long nextVal = (value & mask) >>> remainingBits;
            compressedData[index + 1] |= (int) nextVal;
        }
    }

    /** Unpacks an integer value starting at bitPos (bitCount bits) */
    private long unpackBits(long bitPos, int bitCount) {
        int index = (int) (bitPos / 32);
        int offset = (int) (bitPos % 32);
        long val = Integer.toUnsignedLong(compressedData[index]) >>> offset;
        int remainingBits = 32 - offset;
        if (bitCount > remainingBits && index + 1 < compressedData.length) {
            val |= (Integer.toUnsignedLong(compressedData[index + 1]) << remainingBits);
        }
        long mask = (1L << bitCount) - 1;
        return val & mask;
    }

    /** Returns number of bits required to encode value */
    private int bitWidth(int value) {
        if (value == 0) return 1;
        return 32 - Integer.numberOfLeadingZeros(value);
    }

    @Override
    public String toString() {
        return String.format("BitPackingOverflow(baseBits=%d, overflowBits=%d, entries=%d, overflow=%d)",
                baseBits, overflowBits, originalLength, overflowData.length);
    }
}
