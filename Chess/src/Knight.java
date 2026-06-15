import java.util.ArrayList;

public class Knight extends Piece {

    public Knight(Colors color, int row, int col) {
        super(PieceTypes.knight, color, row, col);
    }

    public void move(int row, int col) {
        setPosition(row, col);
    }

    public ArrayList<int[]> getMoves(Board board, Boolean extraMoves) {
        ArrayList<int[]> moves = new ArrayList<>();
        int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                { 2, 1 } };
        for (int[] move : knightMoves) {

            int newRow = getPosition()[0]+ move[0];
            int newCol = getPosition()[1] + move[1];

            if (board.isInBounds(newRow, newCol) && (board.getPiece(newRow, newCol) == null
                    || board.getPiece(newRow, newCol).getColor() != getColor())) {
                moves.add(new int[] { newRow, newCol });
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
