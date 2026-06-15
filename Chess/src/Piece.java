import java.util.ArrayList;

public abstract class Piece {
    private PieceTypes type;
    private Colors color;
    private int[] position = new int[2];

    private boolean hasMoved = false;

    public Piece(PieceTypes type, Colors color, int row, int col) {
        this.type = type;
        this.color = color;
        position[0] = row;
        position[1] = col;
    }

    public Piece() {
    }

    public enum PieceTypes {
        pawn, rook, knight, bishop, queen, king
    }

    public enum Colors {
        white, black
    }

    public int[] getPosition() {
        return position;
    }

    public void move(int row, int col) { }

    protected void setPosition(int row, int col) {
        position[0] = row;
        position[1] = col;
        hasMoved = true;
    }

    public PieceTypes getType() {
        return type;
    }

    public Colors getColor() {
        return color;
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        return null;
    }

    public boolean getHasMoved() {
        return hasMoved;
    } 
}