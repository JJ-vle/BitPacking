
/*
 * Classe qui gère la décompression / compression (sans chevauchement)
 * + test de base
 */

public class BitPacking {
    private int[] compressedData;
    private int bitWidth;
    private int originalLength;

    /**
     * Calcule nb bits nécessaires pour représenter maxValue.
     */
    private int neededBits(int maxValue) {
        int bits = 32 - Integer.numberOfLeadingZeros(maxValue);
        return Math.max(bits, 1); // au moins 1 bit
    }

    /**
     * Compression (version sans chevauchement)
     */
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
    public int[] decompress() {
        int[] result = new int[originalLength];
        int slotsPerInt = 32 / bitWidth;

        for (int i = 0; i < originalLength; i++) {
            int blockIndex = i / slotsPerInt;
            int offset = (i % slotsPerInt) * bitWidth;

            int mask = (1 << bitWidth) - 1;
            result[i] = (compressedData[blockIndex] >> offset) & mask;
        }

        return result;
    }

    /**
     * Accès direct à l’élément i sans tout décompresser
     */
    public int get(int i) {
        int slotsPerInt = 32 / bitWidth;
        int blockIndex = i / slotsPerInt;
        int offset = (i % slotsPerInt) * bitWidth;

        int mask = (1 << bitWidth) - 1;
        return (compressedData[blockIndex] >> offset) & mask;
    }

    // test de bjase
    public static void main(String[] args) {
        BitPacking bp = new BitPacking();
        int[] original = {1, 2, 3, 10, 15, 7, 8, 31};
        int[] compressed = bp.compress(original);

        System.out.println("Données compressées:");
        for (int c : compressed) {
            System.out.println(Integer.toBinaryString(c));
        }

        System.out.println("\nDécompression:");
        int[] decompressed = bp.decompress();
        for (int d : decompressed) {
            System.out.print(d + " ");
        }

        System.out.println("\n\nAccès direct:");
        System.out.println("Élément 4 = " + bp.get(4));
    }
}
