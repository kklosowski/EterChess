import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    @FXML
    private Label playerTime;
    @FXML
    private Label computerTime;
    @FXML
    private ProgressBar playerTimeProgress;
    @FXML
    private ProgressBar computerTimeProgress;

    private HashMap<String, Long> boardControls = new HashMap<>();

    private Timer[] timers;

//    private Board board = new Board("3k1bn1/p4p2/Ppp1p2p/1n3P1p/3p3P/N1PP4/P1QBPP1B/2Kr2N1 w - - 1 11");
    private Board board;

    private AiPlayer aiPlayer;

    @FXML
    public void initialize() {
         board = new Board();
        showNewGameDialog();
        initPlayer();
        initTimers();
//        startTimer();
        drawBoard(boardCanvas.getGraphicsContext2D());
        drawPieces(root);
    }

    private void initPlayer() {
        if (boardControls.getOrDefault("mode", -1L) == 0L){
            this.aiPlayer = new AiPlayer();
        }
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

                        if ((board.getMoves(selectedPosition) & (targetPosition)) != 0
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

                            //MOVE OF AN AI PLAYER
                            if (aiPlayer != null && !board.movingColor){
                                Platform.runLater(() -> {
                                        aiPlayer.randomMove(board);
                                        drawPieces(root);
                                });
                            }

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
            if (board.allMoves(board.movingColor).isEmpty()){
                showWinDialog(board.movingColor);
            }
        } else {
            kingIV.setEffect(null);
            if (board.allMoves(board.movingColor).isEmpty()){
                showDrawDialog();
            }
        }
//        board.getPinnedPieceMoveMasks(board.movingColor);
//        System.out.println(Conversions.longToGrid(board.getCheckingPieces(board.movingColor)));
//        System.out.println(Conversions.longToGrid(board.checkBlockMask));
    }

    public void showNewGameDialog() {
        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/new_game_dialog.fxml"));
            Parent parent = fxmlLoader.load();
            NewGameDialogController dialogController = fxmlLoader.getController();
            dialogController.setBoardControlsMap(this.boardControls);
            Scene scene = new Scene(parent, 600, 400);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDrawDialog(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game over.");
        alert.setHeaderText("Game ended in a DRAW");
        alert.setContentText("Do you want to play again?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            initialize();
        } else {
            ((Stage)boardCanvas.getScene().getWindow()).close();
        }
    }

    public void showWinDialog(boolean color){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game over.");
        alert.setHeaderText(color ? "White won." : "Black won.");
        alert.setContentText("Do you want to play again?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            initialize();
        } else {
            ((Stage)boardCanvas.getScene().getWindow()).close();
        }
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

    public void initTimers(){
        timers = new Timer[]{new Timer(), new Timer()};
        timers[0].increment = boardControls.get("time");
        timers[1].increment = boardControls.get("time");

        timers[0].timeLeft = boardControls.get("time");
        timers[1].timeLeft = boardControls.get("time");

        playerTimeProgress.setProgress(1d);
        computerTimeProgress.setProgress(1d);

        playerTime.setText(formatTime(timers[0].timeLeft));
        computerTime.setText(formatTime(timers[0].timeLeft));
    }

    public void startTimer(){
        int moving = board.movingColor ? 1 : 0;
        while(timers[moving].timeLeft > 0){
            Platform.runLater(() -> {

                    timers[moving].timeLeft -= 1000;
                    playerTime.setText(formatTime(timers[moving].timeLeft));
            });
        }
//        System.out.println("setting timer " + (color ? "white" : "black"));
    }

    private String formatTime(long millis){
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public void resign(){
        showWinDialog(!board.movingColor);
    }
}