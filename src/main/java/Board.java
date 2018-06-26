import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    final boolean BLACK = false;
    final boolean WHITE = true;
    Map<String, Long> pieces = new HashMap<>();
    Map<String, Character> pieceSymbols = new HashMap<>();
    Map<String, Map<Long, Long>> moves = new HashMap<>();
    Map<String, Long> constants = new HashMap<>();
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

    private String longToString(long n) {
        return StringUtils.leftPad(Long.toBinaryString(n), 64, '0');
    }

    private int squareToBitIndex(String pos) {
        if (!isValidPosition(pos)) {
            throw new IllegalArgumentException("Position isn't valid");
        } else {
            pos = pos.toLowerCase();
            return (pos.charAt(0) - 97) + (pos.charAt(1) - 49) * 8;
        }
    }

    private String posToSquare(int pos) {
        String square = "";
        square += (char)(pos % 8  + 97);
        square += Math.floorDiv(pos, 8) + 1;
        return square;
    }

    private long squareToLong(String square) {
        return 1L << squareToBitIndex(square);
    }

    private long allPieces() {
        final long[] all = {0L};
        pieces.forEach((key, value) -> {
            all[0] &= value;
        });
        return all[0];
    }

    private long colorPieces(boolean color){
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
        System.out.println(all[0]);
        return all[0];
    }

    private long knightMoves(long knights, boolean color) {
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

        return nMoves & ~colorPieces(color);
    }



    @Override
    public String toString() {
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


//    public void drawPieces(GraphicsContext gc){
//        pieces.forEach((key, value) -> {
//            String piece = longToString(value);
//            for (int i = 0; i < piece.length(); i++) {
//                if (piece.charAt(i) == '1') {
//                    gc.drawImage(
//                            new Image("/pieces/" + key.substring(0, key.length() - 1) + ".png",
//                                    100d,
//                                    100d,
//                                    false,
//                                    true
//                            ),
//                            i % 8 * 100,
//                            Math.floorDiv(i, 8) * 100);
//                }
//            }
//        });
//    }

    public void drawPieces(AnchorPane root) {
        pieces.forEach((key, value) -> {

            String piece = longToString(value);
            for (int i = 0; i < piece.length(); i++) {
                if (piece.charAt(piece.length() - 1 - i) == '1') {
                    String type = key.substring(0, key.length() - 1);
                    ImageView p = new ImageView(new Image("/pieces/" + type + ".png",
                            100d,
                            100d,
                            true,
                            true
                    ));
                    p.setX(i % 8 * 100);
                    p.setY(700 - Math.floorDiv(i, 8) * 100);
                    p.setCursor(Cursor.MOVE);
                    p.setId(type + "#" + posToSquare(i));
                    p.setOnMouseClicked(x -> {
                        String pieceId = ((ImageView) x.getSource()).getId();
                        drawMoves(getMoves(pieceId.split("#")[0], pieceId.split("#")[1]), root);
                    });

                    final Delta dragDelta = new Delta();

                    p.setOnMousePressed(mouseEvent -> {
                        dragDelta.x = p.getLayoutX() - mouseEvent.getSceneX();
                        dragDelta.y = p.getLayoutY() - mouseEvent.getSceneY();
                        p.setCursor(Cursor.MOVE);
                    });

                    p.setOnMouseDragged(mouseEvent -> {
                        p.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                        p.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
                    });

                    p.setOnMouseReleased(mouseEvent -> {
                        p.setLayoutX(Math.floorDiv((int) mouseEvent.getSceneX() + 50 + (int) dragDelta.x, 100) * 100f);
                        p.setLayoutY(Math.floorDiv((int) mouseEvent.getSceneY() + 50 + (int) dragDelta.y, 100) * 100);
                        System.out.println(Math.floorDiv((int) mouseEvent.getSceneX(), 100) * 100f);
                        System.out.println((int) mouseEvent.getSceneY() - Math.floorDiv((int) mouseEvent.getSceneY(), 100) * 100f);
                    });

                    root.getChildren().add(p);
                }
            }
        });
    }

    public void drawMoves(long moves, AnchorPane root){
        String mov = longToString(moves);
        Group moveGroup = ((Group) root.getScene().lookup("#moveGroup"));


        moveGroup.getChildren().clear();

        for (int i = 0; i < mov.length(); i++) {
            if (mov.charAt(mov.length() - 1 - i) == '1') {
                Circle c = new Circle(10f, Color.FIREBRICK);
                c.setCenterX(i % 8 * 100 + 50);
                c.setCenterY(700 - Math.floorDiv(i, 8) * 100 + 50);
                moveGroup.toFront();
                moveGroup.getChildren().add(c);
            }
        }
    }

    private long getMoves(String piece, String square){
        boolean color = piece.contains("white");

        if (piece.toLowerCase().contains("knight")) {
            return knightMoves(squareToLong(square), color);
        }
        else return 0L;
    }



    private boolean isValidPosition(String pos) {
        pos = pos.toLowerCase();
        return pos.length() == 2 &&
                pos.charAt(0) >= 'a' &&
                pos.charAt(0) <= 'h' &&
                pos.charAt(1) >= '1' &&
                pos.charAt(1) <= '8';
    }


    private List<Long> rookMoves(String pos, boolean color) {

        return null;
    }

//    private List<Long> possibleMovesToSquare(String pos){
//
//    }

    class Delta { double x, y; }

}
