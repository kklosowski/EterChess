public class Game {
    BoardController boardController;
    TimeControl timeControl;

    public Game(BoardController boardController, TimeControl timeControl) {
        this.boardController = boardController;
        this.timeControl = timeControl;
    }
}
