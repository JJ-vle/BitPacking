public class BitPackingOverlap implements ICompressor {
    private int[] compressedData;
    private int bitWidth;
    private int originalLength;

    private int neededBits(int maxValue) {
        int bits = 32 - Integer.numberOfLeadingZeros(maxValue);
        return Math.max(bits, 1);
    }

    @Override
    public int[] compress(int[] array) {
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

    @Override
    public int[] decompress() {
        int[] result = new int[originalLength]; // ou originalLength selon ta classe
        return decompress(result);
    }

    @Override
    public int[] decompress(int[] output) {
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

            output[i] = value;
            bitPos += bitWidth; // avancer de bitWidth bits dans le flux
        }

        return output;
    }

    @Override
    public int get(int i) {
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
}
