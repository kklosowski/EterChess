import org.apache.commons.lang3.StringUtils;

public class Conversions {
    public static String longToString(long n) {
        return StringUtils.leftPad(Long.toBinaryString(n), 64, '0');
    }

    public static long squareToBitIndex(String pos) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Position isn't valid");
        } else {
            pos = pos.toLowerCase();
            return (pos.charAt(0) - 97) + (pos.charAt(1) - 49) * 8;
        }
    }

    public static String posToSquare(long pos) {
        String square = "";
        square += (char) (pos % 8 + 97);
        square += Math.floorDiv(pos, 8) + 1;
        return square;
    }

    public static long squareToLong(String square) {
        return 1L << squareToBitIndex(square);
    }

    public static long coordToPosition(double x, double y) {
        y = Math.abs(y - 8 * BoardController.SQUARE_HEIGHT);
        return Math.floorDiv((long) x, (long) BoardController.SQUARE_WIDTH) + Math.floorDiv((long) y, (long) BoardController.SQUARE_HEIGHT) * 8;
    }

    public static boolean isValidPosition(String pos) {
        pos = pos.toLowerCase();
        return pos.length() == 2 &&
                pos.charAt(0) >= 'a' &&
                pos.charAt(0) <= 'h' &&
                pos.charAt(1) >= '1' &&
                pos.charAt(1) <= '8';
    }
}
