import org.apache.commons.lang3.StringUtils;
import sun.rmi.server.InactiveGroupException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    Map<String, Long> pieces = new HashMap<>();
    Map<String, Character> pieceSymbols = new HashMap<>();
    Map<String, Map<Long, Long>> moves = new HashMap<>();
    Map<String, Long> constants = new HashMap<>();

    final boolean BLACK = false;
    final boolean WHITE = true;

    long whitePawns;
    long whiteKnights;
    long whiteBishops;
    long whiteRooks;
    long whiteQueens;
    long whiteKing;

    long blackPawns;
    long blackKnights;
    long blackBishops;
    long blackRooks;
    long blackQueens;
    long blackKing;


    public Board() {
        constants.put("aFile", 0x8080808080808080L);
        constants.put("bFile", 0x4040404040404040L);
        constants.put("cFile", 0x2020202020202020L);
        constants.put("dFile", 0x1010101010101010L);
        constants.put("eFile", 0x0808080808080808L);
        constants.put("fFile", 0x0404040404040404L);
        constants.put("gFile", 0x0202020202020202L);
        constants.put("hFile", 0x0101010101010101L);
        constants.put("1Rank", 0x00000000000000FFL);
        constants.put("2Rank", 0x000000000000FF00L);
        constants.put("3Rank", 0x0000000000FF0000L);
        constants.put("4Rank", 0x00000000FF000000L);
        constants.put("5Rank", 0x000000FF00000000L);
        constants.put("6Rank", 0x0000FF0000000000L);
        constants.put("7Rank", 0x00FF000000000000L);
        constants.put("8Rank", 0xFF00000000000000L);

        pieces.put("whitePawns", 0b0000000000000000000000000000000000000000000000001111111100000000L);
//        pieces.put("whiteKnights", 0b0000000000000000000000000000000000000000000000000000000001000010L);
//        pieces.put("whiteBishops", 0b0000000000000000000000000000000000000000000000000000000000100100L);
//        pieces.put("whiteRooks", 0b0000000000000000000000000000000000000000000000000000000010000001L);
//        pieces.put("whiteQueens", 0b0000000000000000000000000000000000000000000000000000000000010000L);
//        pieces.put("whiteKing", 0b0000000000000000000000000000000000000000000000000000000000001000L);
//
//        pieces.put("blackPawns", 0b0000000011111111000000000000000000000000000000000000000000000000L);
//        pieces.put("blackKnights", 0b0100001000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackBishops", 0b0010010000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackRooks", 0b1000000100000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackQueens", 0b0001000000000000000000000000000000000000000000000000000000000000L);
//        pieces.put("blackKing", 0b0000100000000000000000000000000000000000000000000000000000000000L);

        pieceSymbols.put("whitePawns", 'P');
        pieceSymbols.put("whiteBishops", 'B');
        pieceSymbols.put("whiteKnights", 'N');
        pieceSymbols.put("whiteRooks", 'R');
        pieceSymbols.put("whiteQueens", 'Q');
        pieceSymbols.put("whiteKing", 'K');

        pieceSymbols.put("blackPawns", 'p');
        pieceSymbols.put("blackBishops", 'b');
        pieceSymbols.put("blackKnights", 'n');
        pieceSymbols.put("blackRooks", 'r');
        pieceSymbols.put("blackQueens", 'q');
        pieceSymbols.put("blackKing", 'k');


        pieceSymbols.put("moves", '*');
//        pieces.put("moves", knightMoves(pieces.get("whiteKnights") | pieces.get("blackKnights")));

    }

    private String longToString(long n) {
        return StringUtils.leftPad(Long.toBinaryString(n), 64, '0');
    }

    private long allPieces(){
        final long[] all = {0L};
        pieces.forEach((key, value) -> {
            all[0] &= value;
        });
        return all[0];
    }

    private long knightMoves(long knights){
        long nMoves = 0L;

        //Moves to the right
        nMoves |= (knights & ~constants.get("hFile")) >> 17;
        nMoves |= (knights & ~constants.get("hFile")) << 15;
        nMoves |= (knights & ~constants.get("gFile") & ~constants.get("hFile")) >> 10;
        nMoves |= (knights & ~constants.get("gFile") & ~constants.get("hFile")) << 6;

        //Moves to the left
        nMoves |= (knights & ~constants.get("aFile") & ~constants.get("bFile")) << 10;
        nMoves |= (knights & ~constants.get("aFile") & ~constants.get("bFile")) >> 6;
        nMoves |= (knights & ~constants.get("aFile")) << 17;
        nMoves |= (knights & ~constants.get("aFile")) >> 15;
//
        return nMoves;
    }


//    private long pawnMoves =

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("0000000000000000000000000000000000000000000000000000000000000000");
        pieces.forEach((key, value) -> {
            String piece = longToString(value).replace('1', pieceSymbols.get(key));
            for (int i = 0; i < piece.length(); i++) {
                if (piece.charAt(i) != '0') {
                    sb.setCharAt(i, piece.charAt(i));
                }
            }
        });
        return sb.toString().replaceAll("(.{8})", "$1\n");
    }


    private int positionToBitIndex(String pos){
        if (!isValidPosition(pos)){
            throw new IllegalArgumentException("Position isn't valid");
        } else {
            pos = pos.toLowerCase();
            return (pos.charAt(0) - 97) + (pos.charAt(1) - 49) * 8 ;
        }
    }

    private boolean isValidPosition(String pos) {
        pos = pos.toLowerCase();
        return pos.length() == 2 &&
                pos.charAt(0) >= 'a' &&
                pos.charAt(0) <= 'h' &&
                pos.charAt(1) >= '1' &&
                pos.charAt(1) <= '8';
    }



    private List<Long> rookMoves(String pos, boolean color){
        return null;
    }

//    private List<Long> possibleMovesToSquare(String pos){
//
//    }
}
