import java.util.List;
import java.util.Map;
import java.util.Random;

public class AiPlayer {
    public void move(Board board) {
    }

    public void randomMove(Board board){
        for (Map.Entry<String, Long> x : board.getPieces().entrySet()) {
            List<Long> moves = Conversions.separateBits(board.getMoves(x.getValue()));
            if (!moves.isEmpty()) {
                System.out.println("Random AI move");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                board.move(x.getValue(), moves.get(new Random().nextInt(moves.size())));
                return;
            }
        }
    }
}
