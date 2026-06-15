import java.util.ArrayList;

public class Bishop extends Piece {

    public Bishop(Colors color, int row, int col) {
        super(PieceTypes.bishop, color, row, col);
    }

    public void move(int row, int col) {
        setPosition(row, col);
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        ArrayList<int[]> moves = new ArrayList<>();
        for (int a = -1; a < 2; a += 2) {
            for (int b = -1; b < 2; b += 2) {
                for (int i = 1; i < 8; i++) {
                    int newRow = getPosition()[0] + a * i;
                    int newCol = getPosition()[1] + b * i;
                    if (board.isInBounds(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                        moves.add(new int[] { newRow, newCol });
                    } else {
                        if (board.isInBounds(newRow, newCol)
                                && board.getPiece(newRow, newCol).getColor() != getColor()) {
                            moves.add(new int[] { newRow, newCol });
                            break;
                        }
                        break;
                    }
                }
            }
        }
        if (extraMoves) {
            if (board.isKingInCheck(getColor(), board, board.getPieces())) {
                ArrayList<int[]> finalMoves = new ArrayList<>();
                for (int i = 0; i < moves.size(); i++) {
                    if (board.protectsCheck(this, moves.get(i)[0], moves.get(i)[1])) {
                        finalMoves.add(moves.get(i));
                    }
                }
                return finalMoves;
            }
        }
        return moves;
    }
}
