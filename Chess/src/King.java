import java.util.ArrayList;

public class King extends Piece {

    public King(Colors color, int row, int col) {
        super(PieceTypes.king, color, row, col);
    }

    public void move(int row, int col) {
        setPosition(row, col);
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        ArrayList<int[]> moves = new ArrayList<>();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0)
                    continue;
                int newRow = getPosition()[0] + a;
                int newCol = getPosition()[1] + b;
                if (board.isInBounds(newRow, newCol) && (board.getPiece(newRow, newCol) == null
                        || board.getPiece(newRow, newCol).getColor() != getColor())) {
                    moves.add(new int[] { newRow, newCol });
                }
            }
        }
        if (extraMoves && !getHasMoved()) { // Castling
            Colors color = getColor();
            int row = getPosition()[0];
            int kingCol = getPosition()[1];
            int kingsideDest = kingCol + 2;
            int queensideDest = kingCol - 2;
            
            // Queenside castling (use Board logic to handle rook location/orientation)
            if (board.canCastleQueenSide(color)) {
                moves.add(new int[] { row, queensideDest });
            }

            // Kingside castling
            if (board.canCastleKingSide(color)) {
                moves.add(new int[] { row, kingsideDest });
            }
        }
        if (extraMoves) {
            ArrayList<int[]> finalMoves = new ArrayList<>();
            for (int i = 0; i < moves.size(); i++) {
                if (board.protectsCheck(this, moves.get(i)[0], moves.get(i)[1]) && !board
                        .isSquareAttacked(moves.get(i)[0], moves.get(i)[1], getColor(), false, board,
                                board.getPieces())) {
                    finalMoves.add(moves.get(i));
                }
            }
            return finalMoves;
        }
        return moves;
    }
}
