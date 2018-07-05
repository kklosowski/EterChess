import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class Board {

    final double SQUARE_WIDTH = 100d;
    final double SQUARE_HEIGHT = 100d;

    final boolean BLACK = false;
    final boolean WHITE = true;
    Map<String, Long> pieces = new HashMap<>();
    Map<String, Character> pieceSymbols = new HashMap<>();
    Map<String, Long> constants = new HashMap<>();


    public Board() {
        constants.put("aFile", 0x0101010101010101L);
        constants.put("bFile", 0x0202020202020202L);
        constants.put("cFile", 0x0404040404040404L);
        constants.put("dFile", 0x0808080808080808L);
        constants.put("eFile", 0x1010101010101010L);
        constants.put("fFile", 0x2020202020202020L);
        constants.put("gFile", 0x4040404040404040L);
        constants.put("hFile", 0x8080808080808080L);
        constants.put("1Rank", 0x00000000000000FFL);
        constants.put("2Rank", 0x000000000000FF00L);
        constants.put("3Rank", 0x0000000000FF0000L);
        constants.put("4Rank", 0x00000000FF000000L);
        constants.put("5Rank", 0x000000FF00000000L);
        constants.put("6Rank", 0x0000FF0000000000L);
        constants.put("7Rank", 0x00FF000000000000L);
        constants.put("8Rank", 0xFF00000000000000L);
        constants.put("edges", 0xFF818181818181FFL);

        pieces.put("whitePawns", 0b0000000000000000000000000000000000000000000000001111111100000000L);
        pieces.put("whiteKnights", 0b0000000000000000000000000000000000000000000000000000000001000010L);
        pieces.put("whiteBishops", 0b0000000000000000000000000000000000000000000000000000000000100100L);
        pieces.put("whiteRooks", 0b0000000000000000000000000000000000000000000000000000000010000001L);
        pieces.put("whiteQueens", 0b0000000000000000000000000000000000000000000000000000000000001000L);
        pieces.put("whiteKings", 0b0000000000000000000000000000000000000000000000000000000000010000L);

        pieces.put("blackPawns", 0b0000000011111111000000000000000000000000000000000000000000000000L);
        pieces.put("blackKnights", 0b0100001000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackBishops", 0b0010010000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackRooks", 0b1000000100000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackQueens", 0b0000100000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackKings", 0b0001000000000000000000000000000000000000000000000000000000000000L);

        pieceSymbols.put("whitePawns", 'P');
        pieceSymbols.put("whiteBishops", 'B');
        pieceSymbols.put("whiteKnights", 'N');
        pieceSymbols.put("whiteRooks", 'R');
        pieceSymbols.put("whiteQueens", 'Q');
        pieceSymbols.put("whiteKings", 'K');

        pieceSymbols.put("blackPawns", 'p');
        pieceSymbols.put("blackBishops", 'b');
        pieceSymbols.put("blackKnights", 'n');
        pieceSymbols.put("blackRooks", 'r');
        pieceSymbols.put("blackQueens", 'q');
        pieceSymbols.put("blackKings", 'k');


//        pieceSymbols.put("moves", '*');
//        pieces.put("moves", knightMoves(pieces.get("whiteKnights") | pieces.get("blackKnights")));

    }

    public Map<String, Long> getPieces() {
        return pieces;
    }

    private long allPieces() {
        return colorPieces(true) | colorPieces(false);
    }

    private long colorPieces(boolean color) {
        String col;
        if (color) {
            col = "white";
        } else {
            col = "black";
        }
        final long[] all = {0L};
        pieces.entrySet()
                .stream()
                .filter(x -> x.getKey().contains(col))
                .forEach(x -> all[0] |= x.getValue());
        return all[0];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("0000000000000000000000000000000000000000000000000000000000000000");
        pieces.forEach((key, value) -> {
            String piece = Conversions.longToString(value).replace('1', pieceSymbols.get(key));
            for (int i = 0; i < piece.length(); i++) {
                if (piece.charAt(i) != '0') {
                    sb.setCharAt(i, piece.charAt(i));
                }
            }
        });
        return sb.toString().replaceAll("(.{8})", "$1\n");
    }


    public void move(long start, long target) {

        //Takedown
        pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & target) != 0)
                .findFirst()
                .ifPresent(x -> pieces.compute(x.getKey(), (k, v) -> v ^ target));

        //Move the piece
        pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & start) != 0)
                .findFirst()
                .ifPresent(x -> pieces.compute(x.getKey(), (k, v) -> v ^ (start | target)));

        System.out.println(toString());
    }

//
//    public void drawMoves(long moves, AnchorPane root) {
//        String mov = Conversions.longToString(moves);
//        Group moveGroup = ((Group) root.getScene().lookup("#moveGroup"));
//
//        moveGroup.getChildren().clear();
//
//        for (int i = 0; i < mov.length(); i++) {
//            if (mov.charAt(mov.length() - 1 - i) == '1') {
//                Circle c = new Circle(10f, Color.FIREBRICK);
//                c.setCenterX(i % 8 * SQUARE_WIDTH + SQUARE_WIDTH / 2);
//                c.setCenterY((7 - Math.floorDiv(i, 8)) * SQUARE_HEIGHT + SQUARE_HEIGHT / 2);
//                moveGroup.toFront();
//                moveGroup.getChildren().add(c);
//            }
//        }
//    }

    public long getMoves(long square) {
        String pieceType = pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & square) != 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No piece on square"))
                .getKey();

        boolean color = pieceType.contains("white");
        pieceType = pieceType.toLowerCase();

        if (pieceType.contains("knight")) {
            return knightMoves(square, color);
        } else if (pieceType.contains("rook")) {
            return rookMoves(square, color);
        } else if (pieceType.contains("bishop")) {
            return bishopMoves(square, color);
        } else if (pieceType.contains("pawn")) {
            return pawnMoves(square, color);
        } else if (pieceType.contains("king")) {
            return kingMoves(square, color);
        } else if (pieceType.contains("queen")) {
            return queenMoves(square, color);
        } else return 0L;
    }

    private long knightMoves(long positions, boolean color) {
        long moves = 0L;

        //Moves to the right
        moves |= (positions & ~constants.get("aFile")) >> 17;
        moves |= (positions & ~constants.get("aFile")) << 15;
        moves |= (positions & ~constants.get("aFile") & ~constants.get("bFile")) >> 10;
        moves |= (positions & ~constants.get("aFile") & ~constants.get("bFile")) << 6;

        //Moves to the left
        moves |= (positions & ~constants.get("hFile") & ~constants.get("gFile")) << 10;
        moves |= (positions & ~constants.get("hFile") & ~constants.get("gFile")) >> 6;
        moves |= (positions & ~constants.get("hFile")) << 17;
        moves |= (positions & ~constants.get("hFile")) >> 15;

        return moves & ~colorPieces(color);
    }

    private long rookMoves(long positions, boolean color) {
//        return constants.entrySet()
//                .stream()
//                .filter(x -> x.getKey().contains("Rank") || x.getKey().contains("File"))
//                .filter(x -> (x.getValue() & rooks) != 0)
//                .mapToLong(Map.Entry::getValue)
//                .reduce(0L, (x, y) -> (x | y) & ~colorPieces(color));

        long moves = 0L;
        long current = colorPieces(color);
        long opponent = colorPieces(!color);

        //Moves to top
        long temp = positions;
        while ((temp & (constants.get("8Rank") | (current ^ positions) | opponent)) == 0){
            temp <<= 8;
            moves |= temp;
        }

        //Moves to right
        temp = positions;
        while ((temp & (constants.get("hFile") | (current ^ positions) | opponent)) == 0) {
            temp <<= 1;
            moves |= temp;
        }

        //Moves to left
        temp = positions;
        while ((temp & (constants.get("aFile") | (current ^ positions) | opponent)) == 0) {
            temp >>= 1;
            moves |= temp;
        }

        //Moves to bottom
        temp = positions;
        while ((temp & (constants.get("1Rank") | (current ^ positions) | opponent)) == 0) {
            temp >>= 8;
            moves |= temp;
        }

        return moves & ~current;
    }

    private long bishopMoves(long positions, boolean color) {
        long moves = 0L;
        long current = colorPieces(color);
        long opponent = colorPieces(!color);

        //Moves to top-left
        long temp = positions;
        while ((temp & (constants.get("aFile") | constants.get("8Rank")
                | (current ^ positions) | opponent)) == 0 ){
            temp <<= 7;
            moves |= temp;
        }

        //Moves to top-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("8Rank")
                | (current ^ positions) | opponent)) == 0){
            temp <<= 9;
            moves |= temp;
        }

        //Moves to bottom-left
        temp = positions;
        while ((temp & (constants.get("aFile") | constants.get("1Rank")
                | (current ^ positions) | opponent)) == 0){
            temp >>= 9;
            moves |= temp;
        }

        //Moves to bottom-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("1Rank")
                | (current ^ positions) | opponent)) == 0){
            temp >>= 7;
            moves |= temp;
        }

        return moves & ~current;
    }

    private long pawnMoves(long positions, boolean color) {
        long moves = 0L;

        if (color) {
            moves |= (positions << 8);
            if ((positions & constants.get("2Rank")) != 0 &&
                    (allPieces() & (positions << 8)) == 0) {
                moves |= (positions << 16);
            }
        } else {
            moves |= (positions >> 8);
            if ((positions & constants.get("7Rank")) != 0 &&
                    (allPieces() & (positions >> 8)) == 0) {
                moves |= (positions >> 16);
            }
        }

        return (moves & ~allPieces()) | (pawnAttacks(positions, color) & colorPieces(!color));
    }

    private long pawnAttacks(long positions, boolean color){
        long moves = 0L;

        if (color){
            moves |= (positions << 9) & ~constants.get("aFile");
            moves |= (positions << 7) & ~constants.get("hFile");
        } else {
            moves |= (positions >> 9) & ~constants.get("hFile");
            moves |= (positions >> 7) & ~constants.get("aFile");
        }

        return moves & ~colorPieces(color);
    }

    private long queenMoves(long positions, boolean color) {
        return rookMoves(positions, color) | bishopMoves(positions, color);
    }

    private long kingMoves(long positions, boolean color) {
        long moves = 0L;

        moves |= ((positions << 9) | (positions >> 7) | (positions << 1)) & ~constants.get("aFile");
        moves |= ((positions << 7) | (positions >> 9) | (positions >> 1)) & ~constants.get("hFile");
        moves |= (positions << 8) | (positions >> 8);

        return moves & ~colorPieces(color);
    }

//    private List<Long> possibleMovesToSquare(String pos){
//
//    }

}
