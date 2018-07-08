import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    final double SQUARE_WIDTH = 100d;
    final double SQUARE_HEIGHT = 100d;

    final boolean BLACK = false;
    final boolean WHITE = true;
    public boolean movingColor = true;
    Map<String, Long> pieces = new HashMap<>();
    Map<String, Character> pieceSymbols = new HashMap<>();
    Map<String, Long> constants = new HashMap<>();
    Map<Character, Boolean> castle = new HashMap<>();
    long enpassant = 0L;
    int halfmove = 1;
    int fullmove = 0;
    long blackAttacks;
    long whiteAttacks;

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

        constants.put("QCastleMask", 0b0000000000000000000000000000000000000000000000000000000000001110L);
        constants.put("KCastleMask", 0b0000000000000000000000000000000000000000000000000000000001100000L);
        constants.put("qCastleMask", 0b0000111000000000000000000000000000000000000000000000000000000000L);
        constants.put("kCastleMask", 0b0110000000000000000000000000000000000000000000000000000000000000L);
        constants.put("KRookCastleMask", 0b0000000000000000000000000000000000000000000000000000000010100000L);
        constants.put("QRookCastleMask", 0b0000000000000000000000000000000000000000000000000000000000001001L);
        constants.put("kRookCastleMask", 0b1010000000000000000000000000000000000000000000000000000000000000L);
        constants.put("qRookCastleMask", 0b0000100100000000000000000000000000000000000000000000000000000000L);

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

        castle.put('K', true);
        castle.put('Q', true);
        castle.put('k', true);
        castle.put('q', true);

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
        System.out.println(toFEN());
    }

    public Map<String, Long> getPieces() {
        return pieces;
    }

    public Map<String, Long> getConstants() {
        return constants;
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

        for (int i = 0; i < 8; i++) {
            sb.replace(i * 8, (i + 1) * 8, new StringBuilder(sb.substring(i * 8, (i + 1) * 8)).reverse().toString());
        }
        return sb.toString().replaceAll("(.{8})", "$1\n");
    }

    public String toFEN() {
        StringBuilder sb = new StringBuilder();

        int emptyCount = 0;
        for (int i = 63; i >= 0; i -= 8) {
            for (int j = 1; j <= 8; j++) {
                final int s = (i - 8 + j);
                String piece = pieces.entrySet()
                        .stream()
                        .filter(x -> (x.getValue() & 1L << s) != 0)
                        .map(x -> pieceSymbols.get(x.getKey()).toString())
                        .findFirst()
                        .orElse("0");

                if (piece.equals("0")) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append(piece);
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
                emptyCount = 0;
            }
            sb.append("/");
        }
        sb.replace(sb.length() - 1, sb.length(), " ");

        sb.append(movingColor ? "w " : "b ");
        sb.append(castle.get('K') ? 'K' : "");
        sb.append(castle.get('Q') ? 'Q' : "");
        sb.append(castle.get('k') ? 'k' : "");
        sb.append(castle.get('q') ? 'q' : "");
        sb.append(sb.charAt(sb.length() - 1) == ' ' ? "- " : " ");
        sb.append(enpassant == 0 ? "- " : Conversions.longToSquare(enpassant) + " ");
        sb.append(halfmove).append(" ");
        sb.append(fullmove);

        return sb.toString();
    }


    public void move(long start, long target) {

        //Takedown
        pieces.entrySet()
                .stream()
                .filter(x -> {
                    if (x.getKey().contains("Pawn")) {
                        return ((x.getValue() | enpassant) & target) != 0;
                    } else {
                        return (x.getValue() & target) != 0;
                    }
                })
                .findFirst()
                .ifPresent(x -> {
                    if ((target & enpassant) != 0) {
                        if (start > enpassant) {
                            pieces.compute(x.getKey(), (k, v) -> v ^ target << 8);
                        } else {
                            pieces.compute(x.getKey(), (k, v) -> v ^ target >> 8);
                        }
                    } else {
                        pieces.compute(x.getKey(), (k, v) -> v ^ target);
                    }
                    halfmove = 0;
                });

        //Move the piece
        pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & start) != 0)
                .peek(x -> {
                    //Enpassant and promotions
                    if (x.getKey().contains("Pawn")) {
                        halfmove = 0;
                        if ((start << 16) == target) {
                            enpassant = (start << 8);
                        } else if (start >> 16 == target) {
                            enpassant = (start >> 8);
                        } else {
                            enpassant = 0L;
                        }
                        if ((target & constants.get("8Rank")) != 0) {
                            System.out.println("promotion white");
                        }
                        if ((target & constants.get("1Rank")) != 0) {
                            System.out.println("promotion black");
                        }
                    }
                    //Move rook while castling
                    if (x.getKey().contains("King")) {
                        if (x.getKey().contains("white")) {
                            if ((start << 2) == target) {
                                pieces.compute("whiteRooks", (k, v) -> v ^ constants.get("KRookCastleMask"));
                            }
                            if ((start >> 2) == target) {
                                pieces.compute("whiteRooks", (k, v) -> v ^ constants.get("QRookCastleMask"));
                            }
                            castle.replace('K', false);
                            castle.replace('Q', false);
                        } else if (x.getKey().contains("black")) {
                            if ((start << 2) == target) {
                                pieces.compute("blackRooks", (k, v) -> v ^ constants.get("kRookCastleMask"));
                            }
                            if ((start >> 2) == target) {
                                pieces.compute("blackRooks", (k, v) -> v ^ constants.get("qRookCastleMask"));
                            }
                            castle.replace('k', false);
                            castle.replace('q', false);
                        }
                    }
                    //Disable castling if rook moves
                    if (x.getKey().contains("Rook")) {
                            if ((start & Conversions.squareToLong("A1")) != 0){
                                castle.replace('Q', false);
                            } else if ((start & Conversions.squareToLong("H1")) != 0){
                                castle.replace('K', false);
                            } else if ((start & Conversions.squareToLong("A8")) != 0){
                                castle.replace('q', false);
                            } else if ((start & Conversions.squareToLong("H8")) != 0){
                                castle.replace('k', false);
                            }
                        }

                })
                .findFirst()
                .ifPresent(x -> pieces.compute(x.getKey(), (k, v) -> v ^ (start | target)));

        if (movingColor == WHITE) {
            whiteAttacks = allAttacks(WHITE);
        } else {
            blackAttacks = allAttacks(BLACK);
        }

        halfmove++;
        if (!movingColor) fullmove++;
        movingColor = !movingColor;

//        System.out.println(toString());
        System.out.println(toFEN());
    }

    public long getMoves(long square) {
        String pieceType = pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & square) != 0)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No piece on square"))
                .getKey()
                .toLowerCase();

        if (this.movingColor != pieceType.contains("white")) {
            return 0L;
        } else if (pieceType.contains("knight")) {
            return knightMoves(square, this.movingColor);
        } else if (pieceType.contains("rook")) {
            return rookMoves(square, this.movingColor);
        } else if (pieceType.contains("bishop")) {
            return bishopMoves(square, this.movingColor);
        } else if (pieceType.contains("pawn")) {
            return pawnMoves(square, this.movingColor);
        } else if (pieceType.contains("king")) {
            return kingMoves(square, this.movingColor);
        } else if (pieceType.contains("queen")) {
            return queenMoves(square, this.movingColor);
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
        long moves = 0L;
        long current = colorPieces(color);
        long opponent = colorPieces(!color);

        //Moves to top
        long temp = positions;
        while ((temp & (constants.get("8Rank") | (current ^ positions) | opponent)) == 0) {
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
                | (current ^ positions) | opponent)) == 0) {
            temp <<= 7;
            moves |= temp;
        }

        //Moves to top-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("8Rank")
                | (current ^ positions) | opponent)) == 0) {
            temp <<= 9;
            moves |= temp;
        }

        //Moves to bottom-left
        temp = positions;
        while ((temp & (constants.get("aFile") | constants.get("1Rank")
                | (current ^ positions) | opponent)) == 0) {
            temp >>= 9;
            moves |= temp;
        }

        //Moves to bottom-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("1Rank")
                | (current ^ positions) | opponent)) == 0) {
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

        return (moves & ~allPieces()) | (pawnAttacks(positions, color) & (colorPieces(!color) | this.enpassant));
    }

    private long pawnAttacks(long positions, boolean color) {
        long moves = 0L;

        if (color) {
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

        //TODO: Check for checks
        moves |= ((positions << 9) | (positions >> 7) | (positions << 1)) & ~constants.get("aFile");
        moves |= ((positions << 7) | (positions >> 9) | (positions >> 1)) & ~constants.get("hFile");
        moves |= (positions << 8) | (positions >> 8);

        if (color == WHITE) {
            if (castle.get('K') && (constants.get("KCastleMask") & (blackAttacks | colorPieces(WHITE))) == 0) {
                moves |= positions << 2;
            }
            if (castle.get('Q') && (constants.get("QCastleMask") & (blackAttacks | colorPieces(WHITE))) == 0) {
                moves |= positions >> 2;
            }
        } else {
            if (castle.get('k') && (constants.get("kCastleMask") & (whiteAttacks | colorPieces(BLACK))) == 0) {
                moves |= positions << 2;
            }
            if (castle.get('q') && (constants.get("qCastleMask") & (whiteAttacks | colorPieces(BLACK))) == 0) {
                moves |= positions >> 2;
            }
        }

        return moves & ~colorPieces(color);
    }

    private void pgnMove(String move) {

    }

//    boolean long castlingMoves

    public long allAttacks(boolean color) {
        final long[] attacks = {0L};
        String col = color ? "white" : "black";

        pieces.entrySet()
                .stream()
                .filter(x -> x.getKey().contains(col))
                .forEach(x -> {
                    for (int i = 0; i < 64; i++) {
                        if (((1L << i) & x.getValue()) != 0) {
                            if (x.getKey().contains("Pawn")) {
                                attacks[0] |= pawnAttacks(1L << i, color);
                            } else {
                                attacks[0] |= getMoves(1L << i);
                            }
//                            System.out.println(Conversions.longToString(attacks[0]));
                        }
                    }
                });
        return attacks[0];
    }

    //TODO: Check
    private List<Long> possibleMovesToPosition(long position, boolean color) {
        String col = color ? "white" : "black";
        List<Long> possibleMoves = new ArrayList<>();
        pieces.entrySet()
                .stream()
                .filter(x -> x.getKey().contains(col))
                .forEach(x -> {
                    long startPos;
                    for (int i = 0; i < 64; i++) {
                        startPos = 1L << i;
                        if ((startPos & x.getValue()) != 0) {
                            if ((getMoves(startPos) & position) != 0) {
                                possibleMoves.add(startPos);
                            }
                        }
                    }
                });
        return possibleMoves;
    }

    public void promotion(String promotionType, long square, boolean color){
        String col = color ? "white" : "black";
        pieces.compute(col + "Pawns", (k, v) -> v ^ square);
        pieces.compute(promotionType, (k, v) -> v | square);
    }

}
