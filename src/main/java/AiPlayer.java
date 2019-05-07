import java.io.IOException;
import java.util.*;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;

public class AiPlayer {

    MultiLayerNetwork model;


    public AiPlayer() {
        try {
            String simpleMlp = new ClassPathResource("models/chessNN.h5").getFile().getPath();
            this.model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (IOException | InvalidKerasConfigurationException | UnsupportedKerasConfigurationException e) {
            e.printStackTrace();
        }
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

    public void neuralNetworkMove(Board board){
        INDArray features = boardToFeatureMap(board);
        INDArray output = model.output(features);
        double[] predictions = output.toDoubleVector();
        Map<Integer, Double> predictionsMap = new HashMap<>();
        List<Integer> legalMoveIndices = getLegalMoveIndices(board);
        for (int i = 0; i < predictions.length; i++) {
            predictionsMap.put(i, predictions[i]);
        }

        int moveIndex = predictionsMap.entrySet().stream()
                .filter(x -> legalMoveIndices.contains(x.getKey()))
                .min(Map.Entry.comparingByValue())
                .get().getKey();
        Tuple<Long, Long> parsedMove = parseIndexToMove(moveIndex);

        if (board.isPromotion(parsedMove.x, parsedMove.y, board.movingColor)){
            String color = board.movingColor ? "white" : "black";
            board.promotion(color + "Queens", parsedMove.x, parsedMove.y, board.movingColor);
        } else {
            System.out.println("KING IN CHECK: " + board.isInCheck(board.movingColor) + "color:" + board.movingColor);
//            System.out.println(board.toString());
//            System.out.println(Conversions.longToGrid(board.whiteAttacks));
            board.move(parsedMove.x, parsedMove.y);
            System.out.println("AI MOVE");
        }
//        System.out.println(output.maxNumber());
//        getLegalMoveIndices(board).forEach(System.out::println);
    }

    private Tuple<Long, Long> parseIndexToMove(int index){

        int end = index % 64;
        int start = (index - end) / 64;

        return new Tuple<>(1L << start, 1L << end);
    }

    private INDArray boardToFeatureMap(Board board){
        INDArray features = Nd4j.zeros(769);
        for (int i=0; i < 769; i++)
            features.putScalar(new int[] {i}, Math.random() < 0.5 ? 0 : 1);
        return features;
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
