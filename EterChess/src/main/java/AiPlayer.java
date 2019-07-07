import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class AiPlayer {
    public double[] jsonArrayToDoubleArray(int size, JSONArray jsonArray){
        double[] doubleArray = new double[size];
        for (int i = 0; i < size; i++) {
            doubleArray[i] = jsonArray.optDouble(i);
        }
        return doubleArray;
    }

    public void randomMove(Board board){
        for (Map.Entry<String, Long> x : board.getPieces().entrySet()) {
            List<Long> moves = Conversions.separateBits(board.getMoves(x.getValue()));
            if (!moves.isEmpty()) {
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

    public double[] getPredictions(String board){
        JSONArray json = null;
        try {
            json = new JSONArray(IOUtils.toString(new URL("http://127.0.0.1:5000/" + board), Charset.forName("UTF-8")));
            return jsonArrayToDoubleArray(json.length(), json);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void neuralNetworkMove(Board board){
        double[] predictions = getPredictions(boardToInput(board));
        Map<Integer, Double> predictionsMap = new HashMap<>();
        List<Integer> legalMoveIndices = getLegalMoveIndices(board);
        for (int i = 0; i < predictions.length; i++) {
            predictionsMap.put(i, predictions[i]);
        }

        int moveIndex = predictionsMap.entrySet().stream()
                .filter(x -> legalMoveIndices.contains(x.getKey()))
                .max(Map.Entry.comparingByValue())
                .get().getKey();
        Tuple<Long, Long> parsedMove = parseIndexToMove(moveIndex);

        if (board.isPromotion(parsedMove.x, parsedMove.y, board.movingColor)){
            String color = board.movingColor ? "white" : "black";
            board.promotion(color + "Queens", parsedMove.x, parsedMove.y, board.movingColor);
        } else {
            board.move(parsedMove.x, parsedMove.y);
        }
    }

    private String top3Moves(Map<Integer, Double> predictionsMap, List<Integer> legalMoveIndices){
        StringBuilder sb = new StringBuilder();
        predictionsMap.entrySet().stream()
                .filter(x -> legalMoveIndices.contains(x.getKey()))
                .sorted(Map.Entry.comparingByValue())
                .limit(20)
                .forEach(x -> sb.append(Conversions.longToSquare(parseIndexToMove(x.getKey()).x)).append(Conversions.longToSquare(parseIndexToMove(x.getKey()).y)).append(" - ").append(x.getValue()*100).append("  |  "));
        return sb.toString();
    }

    private Tuple<Long, Long> parseIndexToMove(int index){

        int end = index % 64;
        int start = (index - end) / 64;

        return new Tuple<>(1L << start, 1L << end);
    }

    private String boardToInput(Board board){
        return  board.toString().replace("\n","").replace("\r", "");
    }



    private List<Integer> getLegalMoveIndices(Board board){
        Map<Long, Long> movesByPiece = board.movesByPiece(board.movingColor);
        List<Integer> moveIndices = new ArrayList<>();

        movesByPiece.entrySet().stream().forEach(x -> {
            Conversions.separateBits(x.getValue()).stream().map(Conversions::longToBitIndex).forEach(y -> {
                moveIndices.add((int) (Conversions.longToBitIndex(x.getKey()) * 64 + y));
            });
        } );

        return moveIndices;
    }
}
