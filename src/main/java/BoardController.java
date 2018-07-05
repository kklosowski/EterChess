import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.awt.event.MouseEvent;

public class BoardController {

    public static final double SQUARE_WIDTH = 100d;
    public static final double SQUARE_HEIGHT = 100d;

    @FXML
    private Canvas boardCanvas;
    @FXML
    private AnchorPane root;

    private Board board = new Board();

    @FXML
    public void initialize(){
        drawBoard(boardCanvas.getGraphicsContext2D());
        drawPieces(root);
    }

    public void drawBoard(GraphicsContext gc){

        int squareHeight = (int) (boardCanvas.getHeight() / 8);
        int squareWidth = (int) (boardCanvas.getWidth() / 8);
        boolean color = true;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                gc.setFill( color ? Color.MINTCREAM : Color.DARKGRAY);
                if( x != 7){
                    color = !color;
                }
                gc.fillRect(x * squareWidth, y * squareHeight, squareWidth, squareHeight);
            }
        }
    }

    public void drawMoves(long moves, AnchorPane root) {
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
        board.getPieces().forEach((key, value) -> {

            String piece = Conversions.longToString(value);
            Group pieceGroup = new Group();
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
                    p.setId(type + "#" + Conversions.posToSquare(i));

                    final Point startCoord = new Point();
                    final Point dragDelta = new Point();

                    p.setOnMousePressed(mouseEvent -> {
                        startCoord.x = p.getLayoutX();
                        startCoord.y = p.getLayoutY();

                        dragDelta.x = p.getLayoutX() - mouseEvent.getSceneX();
                        dragDelta.y = p.getLayoutY() - mouseEvent.getSceneY();

                        String pieceId = ((ImageView) mouseEvent.getSource()).getId();
                        drawMoves(board.getMoves(Conversions.squareToLong(pieceId.split("#")[1])), root);

                        p.setCursor(Cursor.MOVE);
                    });

                    p.setOnMouseDragged(mouseEvent -> {
                        p.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
                        p.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
                    });

                    p.setOnMouseReleased(mouseEvent -> {
                        long selectedPosition = Conversions.coordToPosition(startCoord.x + 1, startCoord.y + 1);
                        long targetPosition = Conversions.coordToPosition(mouseEvent.getSceneX(), mouseEvent.getSceneY());

                        if ((board.getMoves(1L << selectedPosition) & (1L << targetPosition)) > 0
                                && selectedPosition != targetPosition) {
                            p.setLayoutX(Math.floorDiv((long) mouseEvent.getSceneX(), (long) SQUARE_WIDTH) * SQUARE_WIDTH);
                            p.setLayoutY(Math.floorDiv((long) mouseEvent.getSceneY(), (long) SQUARE_HEIGHT) * SQUARE_HEIGHT);
                            board.move(1L << selectedPosition, 1L << targetPosition);

                            //Remove captured node
                            if (!p.getId().equals(mouseEvent.getPickResult().getIntersectedNode().getId())) {
                                ImageView targetIV = (ImageView) mouseEvent.getPickResult().getIntersectedNode();
                                if (targetIV.getParent() instanceof Group)
                                    ((Group) targetIV.getParent()).getChildren().remove(targetIV);
                            }

                            p.setId(p.getId().split("#")[0] + "#" + Conversions.posToSquare(targetPosition));
                            ((Group) root.getScene().lookup("#moveGroup")).getChildren().clear();

                        } else {
                            p.setLayoutX(startCoord.x);
                            p.setLayoutY(startCoord.y);
                            ((Group) root.getScene().lookup("#moveGroup")).getChildren().clear();
                        }
                    });

                    pieceGroup.getChildren().add(p);
                }
            }
            root.getChildren().add(pieceGroup);
        });
    }

}
