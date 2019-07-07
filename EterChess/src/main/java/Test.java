import java.io.IOException;

public class Test {

    public static void main(String[] args) throws IOException {
        Board board = new Board();
        AiPlayer playerAi = new AiPlayer();
        AiPlayer playerRandom = new AiPlayer();
        for (int game = 0; game < 10; game++) {
            for (int i = 0; i < 300; i++) {
                if (!handleCheck(board)) playerAi.neuralNetworkMove(board);
                else break;

                if (!handleCheck(board)) playerRandom.randomMove(board);
                else break;

                System.out.println(board.toString());
            }
        }
    }

    public static boolean handleCheck(Board board){

        if (board.isInCheck(board.movingColor)) {
            if (board.allMoves(board.movingColor).isEmpty()){
                System.out.println(!board.movingColor + " won");
                return true;
            }
        } else {
            if (board.allMoves(board.movingColor).isEmpty()){
                System.out.println("Draw");
                return true;
            }
        }
        return false;
    }
}
