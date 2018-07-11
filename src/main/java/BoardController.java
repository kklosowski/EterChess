import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.concurrent.atomic.AtomicInteger;

public class BoardController {

    public static final double SQUARE_WIDTH = 100d;
    public static final double SQUARE_HEIGHT = 100d;
    public static final Color DARK_SQUARE_COLOR = Color.DARKGRAY;
    private static final Color LIGHT_SQUARE_COLOR = Color.MINTCREAM;

    @FXML
    private Canvas boardCanvas;
    @FXML
    private AnchorPane root;
    @FXML
    private Group pieceGroup;

    private Board board = new Board();

    @FXML
    public void initialize() {
        drawBoard(boardCanvas.getGraphicsContext2D());
        drawPieces(root);
    }

    public void drawBoard(GraphicsContext gc) {

        int squareHeight = (int) (boardCanvas.getHeight() / 8);
        int squareWidth = (int) (boardCanvas.getWidth() / 8);
        boolean color = true;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                gc.setFill(color ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);
                if (x != 7) {
                    color = !color;
                }
                gc.fillRect(x * squareWidth, y * squareHeight, squareWidth, squareHeight);
            }
        }
    }

    public void drawMoves(long moves) {
        String mov = Conversions.longToString(moves);
        Group moveGroup = ((Group) root.getScene().lookup("#moveGroup"));

        moveGroup.getChildren().clear();

        for (int i = 0; i < mov.length(); i++) {
            if (mov.charAt(mov.length() - 1 - i) == '1') {
                Circle c = new Circle(10f, Color.FIREBRICK);
                c.setCenterX(i % 8 * SQUARE_WIDTH + SQUARE_WIDTH / 2);
                c.setCenterY((7 - Math.floorDiv(i, 8)) * SQUARE_HEIGHT + SQUARE_HEIGHT / 2);
                moveGroup.toFront();
                moveGroup.getChildren().add(c);
            }
        }
    }

    public void drawPieces(AnchorPane root) {

        pieceGroup.getChildren().clear();
        board.getPieces().forEach((key, value) -> {

            String piece = Conversions.longToString(value);
            //TODO: something here
//            Group pieceGroup = new Group();
//            pieceGroup.setId("pieceGroup");

            for (int i = 0; i < piece.length(); i++) {
                if (piece.charAt(piece.length() - 1 - i) == '1') {
                    String type = key.substring(0, key.length() - 1);
                    ImageView p = new ImageView(new Image("/pieces/" + type + ".png",
                            SQUARE_WIDTH,
                            SQUARE_HEIGHT,
                            true,
                            true
                    ));
                    p.setLayoutX(i % 8 * SQUARE_WIDTH);
                    p.setLayoutY(7 * SQUARE_HEIGHT - Math.floorDiv(i, 8) * SQUARE_HEIGHT);
                    p.setCursor(Cursor.MOVE);
//                    p.setId(type + "#" + i);
                    p.setId(type + "#" + Conversions.posToSquare(i));

                    final Point startCoord = new Point();
                    final Point dragDelta = new Point();

                    p.setOnMousePressed(mouseEvent -> {
                        startCoord.x = p.getLayoutX();
                        startCoord.y = p.getLayoutY();

                        dragDelta.x = p.getLayoutX() - mouseEvent.getSceneX();
                        dragDelta.y = p.getLayoutY() - mouseEvent.getSceneY();

                        String pieceId = ((ImageView) mouseEvent.getSource()).getId();
                        drawMoves(board.getMoves(Conversions.squareToLong(pieceId.split("#")[1])));

                        p.setCursor(Cursor.MOVE);

                    });

                    p.setOnMouseDragged(mouseEvent -> {
                        p.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                        p.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
                    });

                    p.setOnMouseReleased(mouseEvent -> {
                        long selectedPosition = 1L << Conversions.coordToPosition(startCoord.x + 1, startCoord.y + 1);
                        long targetPosition = 1L << Conversions.coordToPosition(mouseEvent.getSceneX(), mouseEvent.getSceneY());

                        if ((board.getMoves(selectedPosition) & (targetPosition)) > 0
                                && selectedPosition != targetPosition) {
                            p.setLayoutX(Math.floorDiv((long) mouseEvent.getSceneX(), (long) SQUARE_WIDTH) * SQUARE_WIDTH);
                            p.setLayoutY(Math.floorDiv((long) mouseEvent.getSceneY(), (long) SQUARE_HEIGHT) * SQUARE_HEIGHT);

                            if (type.contains("Pawn")
                                    && (((targetPosition & board.getConstants().get("8Rank")) != 0)
                                    || (targetPosition & board.getConstants().get("1Rank")) != 0)) {
                                drawPromotion(board.movingColor, targetPosition);
                            }

                            board.move(selectedPosition, targetPosition);


                            p.setId(p.getId().split("#")[0] + "#" + Conversions.posToSquare(targetPosition));
                            ((Group) root.getScene().lookup("#moveGroup")).getChildren().clear();

                            drawPieces(root);

                        } else {
                            p.setLayoutX(startCoord.x);
                            p.setLayoutY(startCoord.y);
                            ((Group) root.getScene().lookup("#moveGroup")).getChildren().clear();
                        }
                    });


                    pieceGroup.getChildren().add(p);

                }
            }
        });

        //Check glow
        String king = (board.movingColor ? "white" : "black") + "Kings";
        String kingPos = Conversions.posToSquare(Conversions.longToBitIndex(board.pieces.get(king)));
        ImageView kingIV = (ImageView) pieceGroup.lookup("#" + king.substring(0, king.length() - 1) + "#" + kingPos);

        if (board.isInCheck(board.movingColor)) {
            kingIV.setEffect(new DropShadow(40, Color.RED));
        } else {
            kingIV.setEffect(null);
        }

//        System.out.println(Conversions.longToGrid(board.getCheckingPieces(board.movingColor)));
//        System.out.println(Conversions.longToGrid(board.checkBlockMask));
    }

    public void drawPromotion(boolean color, long square) {
        String col = color ? "white" : "black";
        Pane promotionPane = new Pane();
        promotionPane.setBackground(new Background(new BackgroundFill(Color.STEELBLUE, null, null)));
        AtomicInteger position = new AtomicInteger(0);


        board.getPieces()
                .keySet()
                .stream()
                .filter(x -> x.contains(col) && !x.contains("Pawn") && !x.contains("King"))
                .forEach(type -> {
                    ImageView p = new ImageView(new Image("/pieces/" + type.substring(0, type.length() - 1) + ".png",
                            SQUARE_WIDTH,
                            SQUARE_HEIGHT,
                            true,
                            true
                    ));
                    promotionPane.setLayoutX(200d);
                    promotionPane.setLayoutY(color ? 100d : 600d);
                    p.setLayoutX(SQUARE_WIDTH * position.getAndUpdate(x -> x + 1));
                    p.setLayoutY(0);
                    p.setCursor(Cursor.HAND);
                    p.setId(type + "#Promotion");

                    p.setOnMouseEntered(mouseEvent -> p.setCursor(Cursor.HAND));
                    p.setOnMouseClicked(mouseEvent -> {
                        String promotionType = ((ImageView) mouseEvent.getSource()).getId();
                        board.promotion(promotionType.split("#")[0], square, !board.movingColor);
                        promotionPane.getChildren().clear();
                        drawPieces(root);
                    });
                    promotionPane.getChildren().add(p);
                });
        root.getChildren().add(promotionPane);
    }
}
