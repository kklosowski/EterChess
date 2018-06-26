import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class BoardController {

    @FXML
    private Canvas boardCanvas;
    @FXML
    private AnchorPane root;

    private Board board = new Board();

    @FXML
    public void initialize(){
        drawBoard(boardCanvas.getGraphicsContext2D());
        board.drawPieces(root);
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

    public void drawPieces(){

    }

}
