package old;

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


    
    /////////////////////////////////////////////////////
    // SANS CHEVAUCHEMENT
    /////////////////////////////////////////////////////
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

    /////////////////////////////////////////////////////
    // AVEC CHEVAUCHEMENT
    /////////////////////////////////////////////////////

    public int[] compressOverlap(int[] array) {
        this.originalLength = array.length;

        // Trouver valeur max
        int max = 0;
        for (int v : array) if (v > max) max = v;
        this.bitWidth = neededBits(max);

        // nb total bits nécessaires
        long totalBits = (long) array.length * bitWidth;
        int compressedLength = (int) Math.ceil(totalBits / 32.0);
        compressedData = new int[compressedLength];

        long bitPos = 0; // position courante en bits dans le flux
        for (int value : array) {
            int blockIndex = (int) (bitPos / 32);       // entier cible
            int bitOffset = (int) (bitPos % 32);        // offset dans cet entier

            // combien de bits restent dans ce bloc 32
            int spaceLeft = 32 - bitOffset;

            if (spaceLeft >= bitWidth) {
                compressedData[blockIndex] |= (value << bitOffset);
            } else {
                int lowPart = value & ((1 << spaceLeft) - 1);
                int highPart = value >>> spaceLeft;

                compressedData[blockIndex] |= (lowPart << bitOffset);
                compressedData[blockIndex + 1] |= highPart;
            }
            bitPos += bitWidth;
        }

        return compressedData;
    }

    public int[] decompressOverlap() {
        int[] result = new int[originalLength];
        long bitPos = 0; // position courante dans flux de bits global

        for (int i = 0; i < originalLength; i++) {
            int blockIndex = (int) (bitPos / 32); // indice de l'entier contenant (au moins) une partie de la valeur
            int bitOffset = (int) (bitPos % 32);  // décalage dans cet entier
            int spaceLeft = 32 - bitOffset;       // nb de bits disponibles jusqu'à la fin de cet entier

            int value;
            if (spaceLeft >= bitWidth) { //un bloc
                value = (compressedData[blockIndex] >>> bitOffset) & ((1 << bitWidth) - 1);
            } else { //deux blocs
                // partie basse = récup dans bloc courant
                int lowPart = (compressedData[blockIndex] >>> bitOffset) & ((1 << spaceLeft) - 1);
                // partie haute = récup dans bloc suivant
                int highPart = (compressedData[blockIndex + 1]) & ((1 << (bitWidth - spaceLeft)) - 1);
                // val complète en combiuant haut et bas
                value = (highPart << spaceLeft) | lowPart;
            }

            result[i] = value;
            bitPos += bitWidth; // avancer de bitWidth bits dans le flux
        }

        return result;
    }

    public int getOverlap(int i) {
        long bitPos = (long) i * bitWidth;   // position de début de l'élément i
        int blockIndex = (int) (bitPos / 32);
        int bitOffset = (int) (bitPos % 32);
        int spaceLeft = 32 - bitOffset;

        if (spaceLeft >= bitWidth) { //un blco
            return (compressedData[blockIndex] >>> bitOffset) & ((1 << bitWidth) - 1);
        } else { //deux blocx
            int lowPart = (compressedData[blockIndex] >>> bitOffset) & ((1 << spaceLeft) - 1);
            int highPart = (compressedData[blockIndex + 1]) & ((1 << (bitWidth - spaceLeft)) - 1);
            return (highPart << spaceLeft) | lowPart;
        }
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

        
        // Test avec chevauchement
        int[] comp2 = bp.compressOverlap(original);
        int[] decomp2 = bp.decompressOverlap();
        System.out.println("\nAvec chevauchement:");
        for (int d : decomp2) {
            System.out.print(d + " ");
        }
        System.out.println("\nÉlément 4 = " + bp.getOverlap(4));
    }
}
