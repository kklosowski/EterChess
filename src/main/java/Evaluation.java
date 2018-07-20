public class Evaluation {
    public static void main(String[] args) {
        long nodes = 0L;
        Board board = new Board();
        long startTime = System.currentTimeMillis();

        System.out.println(evaluate(board, 1, 3));

        System.out.println(System.currentTimeMillis() - startTime + " ms");
    }

    public static long evaluate(Board board, int depth, int maxDepth) {
        final long[] currentDepthCount = {board.movesByPiece(board.movingColor).values().stream().mapToLong(Conversions::bitCount).sum()};
        if (depth == maxDepth) {
            return currentDepthCount[0];
        }
        board.movesByPiece(board.movingColor)
                .entrySet()
                .stream()
                .forEach(x -> {
                    Conversions.separateBits(x.getValue())
                            .forEach(y -> {
                                        Board next = new Board(board);
                                        next.move(x.getKey(), y);
                                        currentDepthCount[0] += evaluate(next, depth + 1, maxDepth);
                                    }
                            );
                });

//        if (depth < 4){
//            System.out.println(depth);
//
//        }

        return currentDepthCount[0];
    }
}
