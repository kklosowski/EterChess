public class Game {
    BoardController boardController;
    Timer timer;

    public Game(BoardController boardController, Timer timer) {
        this.boardController = boardController;
        this.timer = timer;
    }
}
