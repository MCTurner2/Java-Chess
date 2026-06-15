import java.util.ArrayList;

public class Rook extends Piece {

    public Rook(Colors color, int row, int col) {
        super(PieceTypes.rook, color, row, col);
    }

    public void move(int row, int col) {
        setPosition(row, col);
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] rookMoves = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int[] dir : rookMoves) {
            for (int i = 1; i < board.getRows(); i++) {
                int newRow = getPosition()[0] + dir[0] * i;
                int newCol = getPosition()[1] + dir[1] * i;
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
