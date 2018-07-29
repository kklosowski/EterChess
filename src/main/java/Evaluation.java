public class Evaluation {
    public static void main(String[] args) {
        long nodes = 0L;
        Board board = new Board("rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2");
        long startTime = System.currentTimeMillis();

        System.out.println(evaluate(board, 1, 5));

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

        if (depth == 3){
            System.out.println(currentDepthCount[0]);
        }

        return currentDepthCount[0];
    }
}
