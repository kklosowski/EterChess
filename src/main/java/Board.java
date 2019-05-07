import java.util.*;

//TODO: FIX: FEN constructor doesn't properly update checks and danger square FEN: "3k1bn1/p4p2/Ppp1p2p/1n3P1p/3p3P/N1PP4/P1QBPP1B/2Kr2N1 w - - 1 11"
//TODO: FIX: knight can move out of the pin
//TODO: FIX: black king can escape check by capturing a protected (not necessarily a checker) piece
//TODO: FIX: kings can check each other and capture each other...

public class Board {

    static final boolean BLACK = false;
    static final boolean WHITE = true;
    boolean movingColor = true;
    Map<String, Long> pieces;
    Map<String, Character> pieceSymbols;
    Map<String, Long> constants;
    Map<Character, Boolean> castle;
    long enpassant = 0L;
    int halfmove = 0;
    int fullmove = 1;
    long blackAttacks;
    long whiteAttacks;
    long whiteKingDanger;
    long blackKingDanger;

    public Board() {
        pieces = new HashMap<>();
        castle = new HashMap<>();
        initConstants();

        pieces.put("whitePawns", 0b0000000000000000000000000000000000000000000000001111111100000000L);
//        pieces.put("whitePawns", 0b0000000000000000000000000000000000000000000000000000000000000000L);
        pieces.put("whiteKnights", 0b0000000000000000000000000000000000000000000000000000000001000010L);
        pieces.put("whiteBishops", 0b0000000000000000000000000000000000000000000000000000000000100100L);
        pieces.put("whiteRooks", 0b0000000000000000000000000000000000000000000000000000000010000001L);
        pieces.put("whiteQueens", 0b0000000000000000000000000000000000000000000000000000000000001000L);
        pieces.put("whiteKings", 0b0000000000000000000000000000000000000000000000000000000000010000L);

        pieces.put("blackPawns", 0b0000000011111111000000000000000000000000000000000000000000000000L);
//        pieces.put("blackKnights", 0b0100001000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackBishops", 0b0010010000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackRooks", 0b1000000100000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackQueens", 0b0000100000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackKings", 0b0001000000000000000000000000000000000000000000000000000000000000L);

        pieces.put("blackQueens", 0b0000000000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackRooks", 0b0000000000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackBishops", 0b0000000000000000000000000000000000000000000000000000000000000000L);
        pieces.put("blackKnights", 0b0000000000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackPawns", 0b0000000000011111000000000000000000000000000000000000000000000000L);

        castle.put('K', true);
        castle.put('Q', true);
        castle.put('k', true);
        castle.put('q', true);

//        pieceSymbols.put("moves", '*');
//        pieces.put("moves", knightMoves(pieces.get("whiteKnights") | pieces.get("blackKnights")));
        System.out.println(toFEN());
    }

    public Board(Board toCopy) {
        initConstants();
        this.pieces = new HashMap<>(toCopy.pieces);
        this.castle = new HashMap<>(toCopy.castle);
        this.movingColor = toCopy.movingColor;
        this.enpassant = toCopy.enpassant;
        this.halfmove = toCopy.halfmove;
        this.fullmove = toCopy.fullmove;
        this.blackAttacks = toCopy.blackAttacks;
        this.whiteAttacks = toCopy.whiteAttacks;
        this.blackKingDanger = toCopy.blackKingDanger;
        this.whiteKingDanger = toCopy.whiteKingDanger;
    }

    public Board(String FEN) {
        pieces = new HashMap<>();
        castle = new HashMap<>();
        initConstants();

        pieces.put("whitePawns", 0L);
        pieces.put("whiteKnights", 0L);
        pieces.put("whiteBishops", 0L);
        pieces.put("whiteRooks", 0L);
        pieces.put("whiteQueens", 0L);
        pieces.put("whiteKings", 0L);
        pieces.put("blackPawns", 0L);
        pieces.put("blackKnights", 0L);
        pieces.put("blackBishops", 0L);
        pieces.put("blackRooks", 0L);
        pieces.put("blackQueens", 0L);
        pieces.put("blackKings", 0L);

        String[] fenSplit = FEN.split(" ");

        movingColor = fenSplit[1].equals("w");
        castle.put('K', fenSplit[2].contains("K"));
        castle.put('Q', fenSplit[2].contains("Q"));
        castle.put('k', fenSplit[2].contains("k"));
        castle.put('q', fenSplit[2].contains("q"));
        enpassant = fenSplit[3].equals("-") ? 0L : Conversions.squareToLong(fenSplit[3]);
        halfmove = Integer.valueOf(fenSplit[4]);
        fullmove = Integer.valueOf(fenSplit[5]);
        blackAttacks = allAttacks(BLACK);
        whiteAttacks = allAttacks(WHITE);
        blackKingDanger = kingDangerSquares(BLACK);
        whiteKingDanger = kingDangerSquares(WHITE);

        String positions = new StringBuilder(fenSplit[0]).reverse().toString();

        StringBuilder sb = new StringBuilder();
        Arrays.stream(positions.split("/")).forEach(x -> {
                    sb.append(new StringBuilder(x).reverse().toString());
                }
        );

        positions = sb.toString();

        for (int i = 1; i <= 8; i++) {
            positions = positions.replace(String.valueOf(i), new String(new char[i]).replace("\0", "*"));
        }
        String finalPositions = positions;
        for (int i = 0; i < 64; i++) {
            if (positions.charAt(i) != '*'){
                int finalI = i;
                String type = pieceSymbols
                        .entrySet()
                        .stream()
                        .filter(x -> x.getValue() == finalPositions.charAt(finalI))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Illegal piece symbol in FEN string"))
                        .getKey();
                pieces.compute(type, (k, v) -> v |= 1L << finalI);
            }
        }
    }

    public void initConstants() {
        this.pieceSymbols = new HashMap<>();
        this.constants = new HashMap<>();

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

    private void pgnMove(String move) {

    }

    public void move(long start, long target) {

        //Takedown
        pieces.entrySet()
                .stream()
                .filter(x -> {
                    if (x.getKey().contains("whitePawns")) {
                        return ((x.getValue() | (enpassant & constants.get("3Rank"))) & target) != 0;
                    } else if (x.getKey().contains("blackPawns")) {
                        return ((x.getValue() | (enpassant & constants.get("6Rank"))) & target) != 0;
                    } else {
                        return (x.getValue() & target) != 0;
                    }
                })
                .findFirst()
                .ifPresent(x -> {
                    if ((target & enpassant) != 0) {
                        if (start > enpassant) {
                            pieces.compute(x.getKey(), (k, v) -> v ^ (target << 8));
                        } else {
                            pieces.compute(x.getKey(), (k, v) -> v ^ (target >>> 8));
                        }
                    } else {
                        pieces.compute(x.getKey(), (k, v) -> v ^ target);
                    }

                    //Updating castle rights on rook takedown
                    if ((target & 0x8100000000000081L) != 0){
                        if (target == (1L << 63)) castle.replace('k', false);
                        else if (target == 1L << 56) castle.replace('q', false);
                        else if (target == 1L << 7) castle.replace('K', false);
                        else if (target == 0L) castle.replace('Q', false);
                    }

                    halfmove = -1;
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
                        } else if (start >>> 16 == target) {
                            enpassant = (start >>> 8);
                        } else {
                            enpassant = 0L;
                        }
                    } else {
                        halfmove++;
                    }
                    //Move rook while castling
                    if (x.getKey().contains("King")) {
                        if (x.getKey().contains("white")) {
                            if ((start << 2) == target) {
                                pieces.compute("whiteRooks", (k, v) -> v ^ constants.get("KRookCastleMask"));
                            }
                            if ((start >>> 2) == target) {
                                pieces.compute("whiteRooks", (k, v) -> v ^ constants.get("QRookCastleMask"));
                            }
                            castle.replace('K', false);
                            castle.replace('Q', false);
                        } else if (x.getKey().contains("black")) {
                            if ((start << 2) == target) {
                                pieces.compute("blackRooks", (k, v) -> v ^ constants.get("kRookCastleMask"));
                            }
                            if ((start >>> 2) == target) {
                                pieces.compute("blackRooks", (k, v) -> v ^ constants.get("qRookCastleMask"));
                            }
                            castle.replace('k', false);
                            castle.replace('q', false);
                        }
                    }
                    //Disable castling if rook moves
                    if (x.getKey().contains("Rook")) {
                        if ((start & Conversions.squareToLong("A1")) != 0) {
                            castle.replace('Q', false);
                        } else if ((start & Conversions.squareToLong("H1")) != 0) {
                            castle.replace('K', false);
                        } else if ((start & Conversions.squareToLong("A8")) != 0) {
                            castle.replace('q', false);
                        } else if ((start & Conversions.squareToLong("H8")) != 0) {
                            castle.replace('k', false);
                        }
                    }

                })
                .findFirst()
                .ifPresent(x -> {
                    pieces.compute(x.getKey(), (k, v) -> v ^ (start | target));
                    if (!x.getKey().contains("Pawn")) {
                        enpassant = 0L;
                    }
                });


        if (movingColor == WHITE) {
            whiteAttacks = allAttacks(WHITE);
            if (isInCheck(BLACK)) {
                long[] protectedCheckers = {0L};
                long checkersBlack = getCheckingPieces(BLACK);
                pieces.entrySet()
                        .stream()
                        .filter(x -> (x.getValue() & checkersBlack) != 0)
                        .forEach(x -> {
                            pieces.compute(x.getKey(), (k, v) -> v ^= checkersBlack);
                            protectedCheckers[0] |= allAttacks(WHITE) & checkersBlack;
                            pieces.compute(x.getKey(), (k, v) -> v ^= checkersBlack);
                        });
                blackKingDanger = kingDangerSquares(BLACK) | protectedCheckers[0];
            } else {
                //Filter for protected piece danger
                blackKingDanger = whiteAttacks | kingDangerSquares(BLACK);
            }

        } else {
            blackAttacks = allAttacks(BLACK);
            if (isInCheck(WHITE)) {
                long[] protectedCheckers = {0L};
                long checkersWhite = getCheckingPieces(WHITE);
                pieces.entrySet()
                        .stream()
                        .filter(x -> (x.getValue() & checkersWhite) != 0)
                        .forEach(x -> {
                            pieces.compute(x.getKey(), (k, v) -> v ^= checkersWhite);
                            protectedCheckers[0] |= allAttacks(BLACK) & checkersWhite;
                            pieces.compute(x.getKey(), (k, v) -> v ^= checkersWhite);
                        });
                whiteKingDanger = kingDangerSquares(WHITE) | protectedCheckers[0];
            } else {
                //Filter for protected piece danger
                whiteKingDanger = blackAttacks | kingDangerSquares(WHITE);
            }
        }

        if (!movingColor) fullmove++;
        movingColor = !movingColor;

//        System.out.println(toString());
//        System.out.println(toFEN());
    }

    public long getMoves(long square) {
        String pieceType = pieces.entrySet()
                .stream()
                .filter(x -> (x.getValue() & square) != 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("empty")
                .toLowerCase();

        boolean color = pieceType.contains("white");
        long moves;

        if (this.movingColor != color | pieceType.equals("empty")) {
            moves = 0L;
        } else if (pieceType.contains("knight")) {
            moves = knightMoves(square, this.movingColor);
        } else if (pieceType.contains("rook")) {
            moves = rookMoves(square, this.movingColor);
        } else if (pieceType.contains("bishop")) {
            moves = bishopMoves(square, this.movingColor);
        } else if (pieceType.contains("pawn")) {
            moves = pawnMoves(square, this.movingColor);
        } else if (pieceType.contains("king")) {
            return kingMoves(square, this.movingColor);
        } else if (pieceType.contains("queen")) {
            moves = queenMoves(square, this.movingColor);
        } else moves = 0L;

        if (isInCheck(movingColor)) {
            if (Conversions.bitCount(getCheckingPieces(movingColor)) > 1) {
                return 0L;
//                if (square == pieces.get("whiteKings") | square == pieces.get("whiteKings")){
//                    moves |= kingMoves(square, movingColor);
//                } else {
//                    moves = 0L;
//                }
            } else {
                moves &= (getCheckingPieces(movingColor) | getCheckBlockSquares(movingColor));
            }
        }

        if (getPinnedPieceMoveMasks(movingColor).containsKey(square)) {
            moves &= getPinnedPieceMoveMasks(movingColor).get(square);
        }

        return moves;
    }

    private long knightMoves(long positions, boolean color) {
        long moves = 0L;

        //Moves to the right
        moves |= (positions & ~constants.get("aFile")) >>> 17;
        moves |= (positions & ~constants.get("aFile")) << 15;
        moves |= (positions & ~constants.get("aFile") & ~constants.get("bFile")) >>> 10;
        moves |= (positions & ~constants.get("aFile") & ~constants.get("bFile")) << 6;

        //Moves to the left
        moves |= (positions & ~constants.get("hFile") & ~constants.get("gFile")) << 10;
        moves |= (positions & ~constants.get("hFile") & ~constants.get("gFile")) >>> 6;
        moves |= (positions & ~constants.get("hFile")) << 17;
        moves |= (positions & ~constants.get("hFile")) >>> 15;

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
            temp >>>= 1;
            moves |= temp;
        }

        //Moves to bottom
        temp = positions;
        while ((temp & (constants.get("1Rank") | (current ^ positions) | opponent)) == 0) {
            temp >>>= 8;
            moves |= temp;
        }

        return moves & ~current;
    }

    private long xRayRookMoves(long positions) {
        long moves = 0L;

        //Moves to top
        long temp = positions;
        while ((temp & constants.get("8Rank")) == 0) {
            temp <<= 8;
            moves |= temp;
        }

        //Moves to right
        temp = positions;
        while ((temp & constants.get("hFile")) == 0) {
            temp <<= 1;
            moves |= temp;
        }

        //Moves to left
        temp = positions;
        while ((temp & constants.get("aFile")) == 0) {
            temp >>>= 1;
            moves |= temp;
        }

        //Moves to bottom
        temp = positions;
        while ((temp & constants.get("1Rank")) == 0) {
            temp >>>= 8;
            moves |= temp;
        }

        return moves;
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
            temp >>>= 9;
            moves |= temp;
        }

        //Moves to bottom-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("1Rank")
                | (current ^ positions) | opponent)) == 0) {
            temp >>>= 7;
            moves |= temp;
        }

        return moves & ~current;
    }

    private long xRayBishopMoves(long positions) {
        long moves = 0L;

        //Moves to top-left
        long temp = positions;
        while ((temp & (constants.get("aFile") | constants.get("8Rank"))) == 0) {
            temp <<= 7;
            moves |= temp;
        }

        //Moves to top-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("8Rank"))) == 0) {
            temp <<= 9;
            moves |= temp;
        }

        //Moves to bottom-left
        temp = positions;
        while ((temp & (constants.get("aFile") | constants.get("1Rank"))) == 0) {
            temp >>>= 9;
            moves |= temp;
        }

        //Moves to bottom-right
        temp = positions;
        while ((temp & (constants.get("hFile") | constants.get("1Rank"))) == 0) {
            temp >>>= 7;
            moves |= temp;
        }

        return moves;
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
            moves |= (positions >>> 8);
            if ((positions & constants.get("7Rank")) != 0 &&
                    (allPieces() & (positions >>> 8)) == 0) {
                moves |= (positions >>> 16);
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
            moves |= (positions >>> 9) & ~constants.get("hFile");
            moves |= (positions >>> 7) & ~constants.get("aFile");
        }

        return moves & ~colorPieces(color);
    }

    private long queenMoves(long positions, boolean color) {
        return rookMoves(positions, color) | bishopMoves(positions, color);
    }

    private long kingMoves(long positions, boolean color) {
        long moves = 0L;
        long kingDanger = color ? whiteKingDanger : blackKingDanger;

        moves |= ((positions << 9) | (positions >>> 7) | (positions << 1)) & ~constants.get("aFile");
        moves |= ((positions << 7) | (positions >>> 9) | (positions >>> 1)) & ~constants.get("hFile");
        moves |= (positions << 8) | (positions >>> 8);


        if ((kingDanger & positions) == 0) {
            if (color == WHITE) {
                if (castle.get('K') && (constants.get("KCastleMask") & (blackAttacks | colorPieces(WHITE))) == 0) {
                    moves |= positions << 2;
                }
                if (castle.get('Q') && (constants.get("QCastleMask") & (blackAttacks | colorPieces(WHITE))) == 0) {
                    moves |= positions >>> 2;
                }
            } else {
                if (castle.get('k') && (constants.get("kCastleMask") & (whiteAttacks | colorPieces(BLACK))) == 0) {
                    moves |= positions << 2;
                }
                if (castle.get('q') && (constants.get("qCastleMask") & (whiteAttacks | colorPieces(BLACK))) == 0) {
                    moves |= positions >>> 2;
                }
            }
        }
        return moves & ~colorPieces(color) & ~kingDanger;
    }

    private long kingMovesWithIllegal(long positions, boolean color){
        long moves = 0L;
        moves |= ((positions << 9) | (positions >>> 7) | (positions << 1)) & ~constants.get("aFile");
        moves |= ((positions << 7) | (positions >>> 9) | (positions >>> 1)) & ~constants.get("hFile");
        moves |= (positions << 8) | (positions >>> 8);
        System.out.println(Conversions.longToGrid(moves));
        return moves;
    }

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
                        }
                    }
                });

        return attacks[0];
    }

    public long kingDangerSquares(boolean color) {
        String kingKey = color ? "whiteKings" : "blackKings";
        String opponentKingKey = !color ? "whiteKings" : "blackKings";
        //Temporarily remove the king to calculate danger squares
        long kingTemp = pieces.get(kingKey);
        pieces.compute(kingKey, (k, v) -> v ^ kingTemp);
        long dangerSquares = allAttacks(!color);
        pieces.compute(kingKey, (k, v) -> v ^ kingTemp);
        dangerSquares |= kingMovesWithIllegal(pieces.get(opponentKingKey), !color);
        return dangerSquares;
    }

    public boolean isInCheck(boolean color) {
        if (color) {
            return (blackAttacks & pieces.get("whiteKings")) != 0;
        } else {
            return (whiteAttacks & pieces.get("blackKings")) != 0;
        }
    }

    private int squareAttackersCount() {
        return 0;
    }

//    //TODO: Check
//    private List<Long> possibleMovesToPosition(long position, boolean color) {
//        String col = color ? "white" : "black";
//        List<Long> possibleMoves = new ArrayList<>();
//        pieces.entrySet()
//                .stream()
//                .filter(x -> x.getKey().contains(col))
//                .forEach(x -> {
//                    long startPos;
//                    for (int i = 0; i < 64; i++) {
//                        startPos = 1L << i;
//                        if ((startPos & x.getValue()) != 0) {
//                            if ((getMoves(startPos) & position) != 0) {
//                                possibleMoves.add(startPos);
//                            }
//                        }
//                    }
//                });
//        return possibleMoves;
//    }

    public void promotion(String promotionType, long startPosition, long targetPosition, boolean color) {

        String col = color ? "white" : "black";
        pieces.compute(col + "Pawns", (k, v) -> v ^ startPosition);
        pieces.compute(promotionType, (k, v) -> v | startPosition);
        move(startPosition, targetPosition);
    }

    public long getCheckingPieces(boolean color) {
        String col = color ? "white" : "black";
        String oppCol = color ? "black" : "white";

        long checkers = 0L;

        checkers |= (queenMoves(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Queens"));
        checkers |= (bishopMoves(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Bishops"));
        checkers |= (rookMoves(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Rooks"));
        checkers |= (knightMoves(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Knights"));
        checkers |= (pawnAttacks(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Pawns"));

        return checkers;
    }

    public long getCheckBlockSquares(boolean color) {
        String col = color ? "white" : "black";
        String oppCol = color ? "black" : "white";

        final long[] blocks = {0L};

        Conversions.separateBits(getCheckingPieces(color)).forEach(x -> {
            if ((x & (pieces.get(oppCol + "Rooks"))) != 0) {
                long rookAttackMask = rookMoves(pieces.get(col + "Kings"), color);
                blocks[0] |= (rookAttackMask & rookMoves(pieces.get(oppCol + "Rooks") & rookAttackMask, !color));
            } else if ((x & (pieces.get(oppCol + "Bishops"))) != 0) {
                long bishopAttackMask = bishopMoves(pieces.get(col + "Kings"), color);
                blocks[0] |= (bishopAttackMask & bishopMoves(pieces.get(oppCol + "Bishops") & bishopAttackMask, !color));
            } else if ((x & (pieces.get(oppCol + "Queens"))) != 0) {
                if ((rookMoves(pieces.get(col + "Kings"), color) & pieces.get(oppCol + "Queens")) != 0) {
                    long rookAttackMask = rookMoves(pieces.get(col + "Kings"), color);
                    blocks[0] |= (rookAttackMask & rookMoves(pieces.get(oppCol + "Queens") & rookAttackMask, !color));
                } else {
                    long bishopAttackMask = bishopMoves(pieces.get(col + "Kings"), color);
                    blocks[0] |= (bishopAttackMask & bishopMoves(pieces.get(oppCol + "Queens") & bishopAttackMask, !color));
                }
            }
        });

        return blocks[0];
    }

    public Map<Long, Long> getPinnedPieceMoveMasks(boolean color) {
        String col = color ? "white" : "black";
        String oppCol = color ? "black" : "white";

        Map<Long, Long> pinnedPieceMoves = new HashMap<>();

        final long[] pinnedPieces = {0L};
        long allMoving = colorPieces(color);
        long allOpponent = colorPieces(!color);
        long allBoth = allMoving | allOpponent;

        long movingKingPos = pieces.get(col + "Kings");


        Conversions.separateBits(pieces.get(oppCol + "Rooks") | pieces.get(oppCol + "Queens")).forEach(r -> {
            if (Conversions.bitCount(((xRayRookMoves(r) & xRayRookMoves(movingKingPos)) & allMoving)) == 1
                    && Conversions.bitCount(((rookMoves(r, !color) & xRayRookMoves(movingKingPos)) & allBoth)) < 2
                    && (xRayRookMoves(r) & movingKingPos) != 0) {

                pinnedPieceMoves.put(allMoving & (rookMoves(r, !color)), (xRayRookMoves(r) & xRayRookMoves(movingKingPos)) | r);
            }
        });


        Conversions.separateBits(pieces.get(oppCol + "Bishops") | pieces.get(oppCol + "Queens")).forEach(b -> {
            if (Conversions.bitCount(((xRayBishopMoves(b) & xRayBishopMoves(movingKingPos)) & allMoving)) == 1
                    && Conversions.bitCount(((bishopMoves(b, !color) & xRayBishopMoves(movingKingPos)) & allBoth)) < 2
                    && (xRayBishopMoves(b) & movingKingPos) != 0) {

                pinnedPieceMoves.put(allMoving & (bishopMoves(b, !color)), (xRayBishopMoves(b) & xRayBishopMoves(movingKingPos)) | b);
            }
        });

//        return pinnedPieces[0];
        return pinnedPieceMoves;
    }

    public List<Long> allMoves(boolean color) {
        List<Long> allMoves = new ArrayList<>();

        String col = color ? "white" : "black";

        pieces.entrySet()
                .stream()
                .filter(x -> x.getKey().contains(col))
                .forEach(x -> {
                    Conversions.separateBits(x.getValue())
                            .forEach(y -> {
                                long mov = getMoves(y);
                                if (mov != 0L) {
                                    allMoves.add(mov);
                                }
                            });
                });

        return allMoves;
    }

    public Map<Long, Long> movesByPiece(boolean color) {
        Map<Long, Long> movesByPiece = new HashMap<>();

        String col = color ? "white" : "black";

        pieces.entrySet()
                .stream()
                .filter(x -> x.getKey().contains(col))
                .forEach(x -> {
                    Conversions.separateBits(x.getValue())
                            .forEach(y -> {
                                long mov = getMoves(y);
                                if (mov != 0L) {
                                    movesByPiece.put(y, mov);
                                }
                            });
                });

//        if (movesByPiece.size() == 0){
//            System.out.println("Checkmate");
//        }
//        if (isInCheck(movingColor)){
//            System.out.println("Check");
//        }

        return movesByPiece;
    }

    public boolean isPromotion(long x, long y, boolean color) {
        String col = color ? "white" : "black";
        return (pieces.get(col + "Pawns") & x) != 0L && (constants.get("1Rank") & y) != 0L;
    }
}
