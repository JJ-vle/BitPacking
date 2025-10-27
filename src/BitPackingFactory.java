public class BitPackingFactory {
    public static ICompressor create(String type) {
        return switch (type.toLowerCase()) {
            case "overlap" -> new BitPackingOverlap();
            case "nooverlap" -> new BitPackingNoOverlap();
            case "overflow" -> new BitPackingOverflow();
            default -> throw new IllegalArgumentException("Unknown compression type: " + type);
        };
    }
}
