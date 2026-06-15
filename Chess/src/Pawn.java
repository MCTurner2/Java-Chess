import java.util.ArrayList;

public class Pawn extends Piece {
    private boolean movedTwoLast = false;

    public Pawn(Colors color, int row, int col) {
        super(PieceTypes.pawn, color, row, col);
    }

    public void move(int row, int col) {
        if (Math.max(row, getPosition()[0]) - Math.min(row, getPosition()[0]) == 2) {
            movedTwoLast = true;
        } else {
            movedTwoLast = false;
        }
        setPosition(row, col);
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        ArrayList<int[]> moves = new ArrayList<>();
        int direction = 0;
        if (getColor() == Colors.white) { 
            if (board.isWhiteIsOnBottom()) {
                direction = -1;
            } else {
                direction = 1;
            }
        } else {
            if (board.isWhiteIsOnBottom()) {
                direction = 1;
            } else {
                direction = -1;
            }
        }
        if (board.isInBounds(getPosition()[0] + direction, getPosition()[1])
                && board.getPiece(getPosition()[0] + direction, getPosition()[1]) == null) {
            moves.add(new int[] { getPosition()[0] + direction, getPosition()[1] });
        }
        if (extraMoves) {
            if (!getHasMoved()) {
                if (board.getPiece(getPosition()[0] + 2 * direction, getPosition()[1]) == null
                        && board.isInBounds(getPosition()[0] + 2 * direction, getPosition()[1])) {
                    moves.add(new int[] { getPosition()[0] + 2 * direction, getPosition()[1] });
                }
            }
            Pawn pawn = null;
            if (board.isInBounds(getPosition()[0], getPosition()[1] - 1) && board.getPiece(getPosition()[0], getPosition()[1] - 1) != null
                    && board.getPiece(getPosition()[0], getPosition()[1] - 1).getType() == PieceTypes.pawn
                    && board.getPiece(getPosition()[0], getPosition()[1] - 1).getColor() != getColor()) {
                pawn = (Pawn) board.getPiece(getPosition()[0], getPosition()[1] - 1);
            }
            if (board.isInBounds(getPosition()[0], getPosition()[1] + 1) && board.getPiece(getPosition()[0], getPosition()[1] + 1) != null
                    && board.getPiece(getPosition()[0], getPosition()[1] + 1).getType() == PieceTypes.pawn
                    && board.getPiece(getPosition()[0], getPosition()[1] + 1).getColor() != getColor()) {
                pawn = (Pawn) board.getPiece(getPosition()[0], getPosition()[1] + 1);
            }
            if (pawn != null && pawn.getMovedTwoLast()) {
                int enPassantRow = getPosition()[0] + direction;
                int enPassantCol = pawn.getPosition()[1];
                if (board.isInBounds(enPassantRow, enPassantCol)
                        && board.getPiece(enPassantRow, enPassantCol) == null) {
                    moves.add(new int[] { enPassantRow, enPassantCol });
                }
            }
        }
        if (board.isInBounds(getPosition()[0] + direction, getPosition()[1] - 1) // attack left
                && board.getPiece(getPosition()[0] + direction, getPosition()[1] - 1) != null
                && board.getPiece(getPosition()[0] + direction, getPosition()[1] - 1).getColor() != getColor()) {
            moves.add(new int[] { getPosition()[0] + direction, getPosition()[1] - 1 });
        }
        if (board.isInBounds(getPosition()[0] + direction, getPosition()[1] + 1) // attack right
                && board.getPiece(getPosition()[0] + direction, getPosition()[1] + 1) != null
                && board.getPiece(getPosition()[0] + direction, getPosition()[1] + 1).getColor() != getColor()) {
            moves.add(new int[] { getPosition()[0] + direction, getPosition()[1] + 1 });
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

    public boolean getMovedTwoLast() {
        return movedTwoLast;
    }

    public void clearMovedTwoLast() {
        movedTwoLast = false;
    }
}
