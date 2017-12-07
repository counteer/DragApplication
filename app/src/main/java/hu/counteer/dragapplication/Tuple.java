package hu.counteer.dragapplication;

public class Tuple {
    private int playerColor;
    private int boardElementColor;

    public Tuple(int playerColor, int boardElementColor) {
        this.playerColor = playerColor;
        this.boardElementColor = boardElementColor;
    }

    public int getPlayerColor() {
        return playerColor;
    }

    public int getBoardElementColor() {
        return boardElementColor;
    }
}
