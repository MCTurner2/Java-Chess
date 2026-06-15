public class Move {
    private int fromX;
    private int fromY;
    private int newX;
    private int newY;

    public Move(int fromX, int fromY, int newX, int newY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.newX = newX;
        this.newY = newY;
    }

    public int getFromX() {
        return fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public int getNewX() {
        return newX;
    }

    public int getNewY() {
        return newY;
    }
}