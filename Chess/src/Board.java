import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Color;

public class Board {
    private int rows = 8;
    private int cols = 8;
    private int cellSize;
    private Piece[][] board;
    private Piece selectedPiece = null;
    private Piece lastPiece = null;
    private boolean whiteTurn = true; // white always starts
    private DrawingPanel drawingPanel;
    private boolean whiteIsOnBottom = true;
    private ArrayList<String> positionHistory = new ArrayList<>();
    private boolean gameOver = false;

    public Board(int width, int height, DrawingPanel drawingPanel, Piece.Colors bottomColor) {
        this.cellSize = Math.min(width, height) / Math.max(rows, cols);
        this.board = new Piece[rows][cols];
        this.drawingPanel = drawingPanel;
        this.whiteIsOnBottom = (bottomColor == Piece.Colors.white);
        resetBoard(drawingPanel);
    }

    public Piece[][] getPieces() {
        return board;
    }

    private void setPieces(Piece[][] newBoard) {
        board = newBoard;
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    public void addPiece(Piece piece) {
        int row = piece.getPosition()[0];
        int col = piece.getPosition()[1];

        board[row][col] = piece;
    }

    public Piece getPiece(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return board[row][col];
        }
        return null;
    }

    public Piece getPiece(Piece.Colors color, Piece.PieceTypes type) { // finds a piece of the given color and typek
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Piece p = board[r][c];
                if (p != null && p.getType() == type && p.getColor() == color) {
                    return p;
                }
            }
        }
        return null;
    }

    public int getCellSize() {
        return cellSize;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(Piece piece) {
        this.selectedPiece = piece;
    }

    public Piece getLastPiece() {
        return lastPiece;
    }

    public void setLastPiece(Piece piece) {
        this.lastPiece = piece;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void switchTurn() {
        whiteTurn = !whiteTurn;
    }

    public boolean isWhiteIsOnBottom() {
        return whiteIsOnBottom;
    }

    public void resetBoard(DrawingPanel drawingPanel) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = null;
            }
        }
        if (!whiteIsOnBottom) {
            for (int i = 0; i < 2; i++) {
                Piece.Colors color = (i == 0) ? Piece.Colors.white : Piece.Colors.black;
                int row = i * 7;
                int queenCol = ((row + 3) % 2 == (color == Piece.Colors.white ? 0 : 1)) ? 3 : 4;
                int kingCol = (queenCol == 3) ? 4 : 3;
                board[row][0] = new Rook(color, row, 0);
                board[row][1] = new Knight(color, row, 1);
                board[row][2] = new Bishop(color, row, 2);
                board[row][queenCol] = new Queen(color, row, queenCol);
                board[row][kingCol] = new King(color, row, kingCol);
                board[row][5] = new Bishop(color, row, 5);
                board[row][6] = new Knight(color, row, 6);
                board[row][7] = new Rook(color, row, 7);
            }
            for (int i = 0; i < cols; i++) {
                board[1][i] = new Pawn(Piece.Colors.white, 1, i);
                board[6][i] = new Pawn(Piece.Colors.black, 6, i);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                Piece.Colors color = (i == 0) ? Piece.Colors.black : Piece.Colors.white;
                int row = i * 7;
                int queenCol = ((row + 3) % 2 == (color == Piece.Colors.white ? 0 : 1)) ? 3 : 4; // queen goes on its own color
                int kingCol = (queenCol == 3) ? 4 : 3;
                board[row][0] = new Rook(color, row, 0);
                board[row][1] = new Knight(color, row, 1);
                board[row][2] = new Bishop(color, row, 2);
                board[row][queenCol] = new Queen(color, row, queenCol);
                board[row][kingCol] = new King(color, row, kingCol);
                board[row][5] = new Bishop(color, row, 5);
                board[row][6] = new Knight(color, row, 6);
                board[row][7] = new Rook(color, row, 7);
            }
            for (int i = 0; i < cols; i++) {
                board[6][i] = new Pawn(Piece.Colors.white, 6, i);
                board[1][i] = new Pawn(Piece.Colors.black, 1, i);
            }
        }
        if (!whiteIsOnBottom) {  
        }
        recordCurrentPosition();
    }

    private void recordCurrentPosition() {
        int[] boardBinary = BinaryBoard.getBoardBinaryArray(this);
        positionHistory.add(Arrays.toString(boardBinary));
    }

    private void resetPawnEnPassantInfo() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Piece piece = board[r][c];
                if (piece instanceof Pawn) {
                    ((Pawn) piece).clearMovedTwoLast();
                }
            }
        }
    }

    public int getRepetitionCount(int[] boardBinary) {
        String position = Arrays.toString(boardBinary);
        int count = 0;
        for (String pastPosition : positionHistory) {
            if (pastPosition.equals(position))
                count++;
        }
        return count;
    }

    public ArrayList<String> getRepetitionHistory() {
        return new ArrayList<>(positionHistory);
    }

    public String isGameOver() {
        if (isCheckmate(Piece.Colors.white, this, board)) {
            System.out.println("Black wins by checkmate!");
            return "Black wins by checkmate!";
        }
        if (isCheckmate(Piece.Colors.black, this, board)) {
            System.out.println("White wins by checkmate!");
            return "White wins by checkmate!";
        }
        if (getRepetitionCount(BinaryBoard.getBoardBinaryArray(this)) >= 3) {
            System.out.println("Game drawn by threefold repetition!");
            return "Game drawn by threefold repetition!";
        }
        if (gethasMoves(Piece.Colors.white) || gethasMoves(Piece.Colors.black)) {
            return null;
        }
        System.out.println("Game drawn by stalemate!");
        return "Game drawn by stalemate!";
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Piece[][] copyBoard(DrawingPanel drawingPanel) {
        Piece[][] copy = new Piece[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                copy[i][j] = board[i][j];
            }
        }
        return copy;
    }

    public void setBoard(Piece[][] newBoard) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (newBoard[i][j] != null) {
                    if (newBoard[i][j].getType() == Piece.PieceTypes.pawn) {
                        board[i][j] = new Pawn(newBoard[i][j].getColor(), i, j);
                    } else if (newBoard[i][j].getType() == Piece.PieceTypes.rook) {
                        board[i][j] = new Rook(newBoard[i][j].getColor(), i, j);
                    } else if (newBoard[i][j].getType() == Piece.PieceTypes.knight) {
                        board[i][j] = new Knight(newBoard[i][j].getColor(), i, j);
                    } else if (newBoard[i][j].getType() == Piece.PieceTypes.bishop) {
                        board[i][j] = new Bishop(newBoard[i][j].getColor(), i, j);
                    } else if (newBoard[i][j].getType() == Piece.PieceTypes.queen) {
                        board[i][j] = new Queen(newBoard[i][j].getColor(), i, j);
                    } else if (newBoard[i][j].getType() == Piece.PieceTypes.king) {
                        board[i][j] = new King(newBoard[i][j].getColor(), i, j);
                    } else {
                        board[i][j] = null;
                    }
                }
            }
        }
    }

    public boolean gethasMoves(Piece.Colors color) { // checks if the given color has any valid moves
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j] != null && board[i][j].getColor() == color) {
                    ArrayList<int[]> moves = board[i][j].getMoves(this, true);
                    if (moves != null && !moves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean protectsCheck(Piece piece, int newRow, int newCol) {// checks if moving the piece to the new position
                                                                       // would leave the king in check
        Piece[][] preMove = new Piece[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                preMove[i][j] = board[i][j];
            }
        }
        preMove[newRow][newCol] = piece;
        preMove[piece.getPosition()[0]][piece.getPosition()[1]] = null;

        Board newBoard = new Board(rows, cols, drawingPanel, whiteIsOnBottom ? Piece.Colors.white : Piece.Colors.black);
        newBoard.setPieces(preMove);

        if (isKingInCheck((whiteTurn) ? Piece.Colors.white : Piece.Colors.black, newBoard, preMove)) {
            return false;
        }
        return true;
    }

    public boolean isValidMove(Piece piece, int newRow, int newCol, boolean extraMoves) { // checks if the move is valid
                                                                                          // for the piece and doesn't
                                                                                          // leave the king in check
        if (piece == null) {
            return false;
        }
        ArrayList<int[]> validMoves = piece.getMoves(this, extraMoves);
        for (int[] move : validMoves) {
            if (move[0] == newRow && move[1] == newCol && protectsCheck(piece, newRow, newCol)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCastlingPossible(Piece king, Piece rook) {
        if (king == null || king.getType() != Piece.PieceTypes.king || king.getHasMoved()) {
            return false;
        }
        if (rook == null || rook.getType() != Piece.PieceTypes.rook || rook.getHasMoved()) {
            return false;
        }
        if (rook.getColor() != king.getColor()) {
            return false;
        }
        if (rook.getPosition()[0] != king.getPosition()[0]) {
            return false;
        }
        return true;
    }

    public boolean canCastleKingSide(Piece.Colors color) {
        Piece king = getPiece(color, Piece.PieceTypes.king);
        if (king == null) return false;
        int row = king.getPosition()[0];
        int kingCol = king.getPosition()[1];
        // find rook to the right of the king
        Piece rook = null;
        int rookCol = -1;
        for (int c = kingCol + 1; c < cols; c++) {
            if (board[row][c] != null && board[row][c].getType() == Piece.PieceTypes.rook
                    && board[row][c].getColor() == color) {
                rook = board[row][c];
                rookCol = c;
                break;
            }
        }
        if (!isCastlingPossible(king, rook)) {
            return false;
        }
        int step = (rookCol > kingCol) ? 1 : -1;
        // squares between king and rook must be empty
        for (int c = kingCol + step; c != rookCol; c += step) {
            if (board[row][c] != null) return false;
        }
        // squares the king passes through (start, one step, destination) must not be attacked
        for (int i = 0; i <= 2; i++) {
            int c = kingCol + i * step;
            if (isSquareAttacked(row, c, king.getColor(), false, this, board)) return false;
        }
        return true;
    }

    public boolean canCastleQueenSide(Piece.Colors color) {
        Piece king = getPiece(color, Piece.PieceTypes.king);
        if (king == null) return false;
        int row = king.getPosition()[0];
        int kingCol = king.getPosition()[1];
        // find rook to the left of the king
        Piece rook = null;
        int rookCol = -1;
        for (int c = kingCol - 1; c >= 0; c--) {
            if (board[row][c] != null && board[row][c].getType() == Piece.PieceTypes.rook
                    && board[row][c].getColor() == color) {
                rook = board[row][c];
                rookCol = c;
                break;
            }
        }
        if (!isCastlingPossible(king, rook)) {
            return false;
        }
        int step = (rookCol > kingCol) ? 1 : -1;
        // squares between king and rook must be empty
        for (int c = kingCol + step; c != rookCol; c += step) {
            if (board[row][c] != null) return false;
        }
        // squares the king passes through (start, one step, destination) must not be attacked
        for (int i = 0; i <= 2; i++) {
            int c = kingCol + i * step;
            if (isSquareAttacked(row, c, king.getColor(), false, this, board)) return false;
        }
        return true;
    }

    public int[] getEnPassantTarget(Piece.Colors color) { // returns the square that can be captured en passant if the
                                                          // last move was a pawn moving two spaces
        int direction = 0;
        if (color == Piece.Colors.white) {
            direction = whiteIsOnBottom ? -1 : 1;
        } else {
            direction = whiteIsOnBottom ? 1 : -1;
        }
        if (lastPiece != null && lastPiece.getType() == Piece.PieceTypes.pawn && lastPiece.getColor() != color) {
            // if the last move was a pawn moving two spaces, add the en passant square to
            // the list of possible moves
            Pawn pawn = (Pawn) lastPiece;
            if (!pawn.getMovedTwoLast()) {
                return null;
            }
            int row = lastPiece.getPosition()[0];
            int col = lastPiece.getPosition()[1];
            int targetRow = row + direction;
            if (!isInBounds(targetRow, col) || board[targetRow][col] != null) {
                return null;
            }
            if (isInBounds(row, col - 1) && board[row][col - 1] != null
                    && board[row][col - 1].getType() == Piece.PieceTypes.pawn
                    && board[row][col - 1].getColor() == color) {
                return new int[] { targetRow, col };
            }
            if (isInBounds(row, col + 1) && board[row][col + 1] != null
                    && board[row][col + 1].getType() == Piece.PieceTypes.pawn
                    && board[row][col + 1].getColor() == color) {
                return new int[] { targetRow, col };
            }
        }
        return null;
    }

    public boolean movePiece(Piece piece, int newRow, int newCol) {
        if (!isValidMove(piece, newRow, newCol, true)) {
            return false;
        }

        resetPawnEnPassantInfo();

        int oldRow = piece.getPosition()[0];
        int oldCol = piece.getPosition()[1];

        if (piece.getType() == Piece.PieceTypes.pawn) { // en passant capture
            if (Math.abs(newCol - oldCol) == 1 && board[newRow][newCol] == null) {
                board[oldRow][newCol] = null;
            }
        }

        if (piece.getType() == Piece.PieceTypes.king) { // moves the rook during castling
            if (Math.abs(newCol - oldCol) != 1 && Math.abs(newRow - oldRow) != 1) { // if the king moves two spaces it's
                                                                                    // castling
                int direction = (newCol > oldCol) ? 1 : -1;
                // find the rook in the direction of movement
                Piece rook = null;
                int rookCol = -1;
                for (int c = oldCol + direction; c >= 0 && c < cols; c += direction) {
                    if (board[oldRow][c] != null && board[oldRow][c].getType() == Piece.PieceTypes.rook
                            && board[oldRow][c].getColor() == piece.getColor()) {
                        rook = board[oldRow][c];
                        rookCol = c;
                        break;
                    }
                }
                int rookDest = newCol - direction; // rook goes to the square adjacent to king's destination
                if (rook != null && rookCol != -1) {
                    board[oldRow][rookDest] = rook;
                    board[oldRow][rookCol] = null;
                    rook.move(oldRow, rookDest);
                }
            }
        }
        board[newRow][newCol] = piece;
        board[oldRow][oldCol] = null;
        piece.move(newRow, newCol);
        if (piece.getType() == Piece.PieceTypes.pawn) { // pawn promotion
            if (piece.getColor() == Piece.Colors.white && newRow == 0 && whiteIsOnBottom
                    || piece.getColor() == Piece.Colors.white && newRow == 7 && !whiteIsOnBottom
                    || piece.getColor() == Piece.Colors.black && newRow == 0 && !whiteIsOnBottom
                    || piece.getColor() == Piece.Colors.black && newRow == 7 && whiteIsOnBottom) {
                piece = new Queen(piece.getColor(), newRow, newCol);
                board[newRow][newCol] = piece;
            }
        }
        lastPiece = piece;
        recordCurrentPosition();
        return true;
    }

    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    public boolean isCheckmate(Piece.Colors color, Board thisBoard, Piece[][] board) { // checks if the given color is
                                                                                       // in checkmate
        if (!isKingInCheck(color, thisBoard, board)) {
            return false;
        }
        if (!gethasMoves(color)) {
            return true;
        }
        return false;
    }

    public boolean isKingInCheck(Piece.Colors color, Board thisBoard, Piece[][] board) { // finds the king of the given
                                                                                         // color and checks if it's
                                                                                         // attacked
        int kingRow = -1;
        int kingCol = -1;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Piece p = board[r][c];
                if (p != null && p.getType() == Piece.PieceTypes.king && p.getColor() == color) {
                    kingRow = r;
                    kingCol = c;
                    return isSquareAttacked(kingRow, kingCol, color, false, thisBoard, board);
                }
            }
            if (kingCol != -1) {
                break;
            }
        }
        return false;
    }

    public boolean wouldKingPassThroughCheck(int startRow, int startCol, int endRow, int endCol, Piece.Colors color) {
        int step = (endCol > startCol) ? 1 : -1;
        for (int c = startCol; c != endCol + step; c += step) { // checks if the king would pass through check during
                                                                // castling
            if (isSquareAttacked(startRow, c, color, false, this, board)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSquareAttacked(int row, int col, Piece.Colors color, boolean extraMoves, Board thisBoard,
            Piece[][] board) { // checks if the square is attacked by any enemy piece by checking all enemy
                               // pieces and their moves
        Piece.Colors enemy = (color == Piece.Colors.white) ? Piece.Colors.black : Piece.Colors.white;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == enemy) {
                    ArrayList<int[]> moves = p.getMoves(thisBoard, false);
                    for (int[] m : moves) {
                        if (m[0] == row && m[1] == col) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void drawBoard(DrawingPanel drawingPanel, ArrayList<int[]> highlightedMoves) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * cellSize;
                int y = i * cellSize;
                boolean isHighlighted = false;
                if (highlightedMoves != null) { // highlights the squares in highlightedMoves
                    for (int[] move : highlightedMoves) {
                        if (move[0] == i && move[1] == j) {
                            isHighlighted = true;
                            break;
                        }
                    }
                }
                if ((i + j) % 2 == 0) { // every other square is grey but mirrors based on whiteIsOnBottom
                    drawingPanel.getGraphics().setColor(new Color(240, 217, 181));
                } else {
                    drawingPanel.getGraphics().setColor(new Color(181, 136, 99));
                }
                drawingPanel.getGraphics().fillRect(x, y, cellSize, cellSize);

                if (isHighlighted) { // highlights the square if it's in highlightedMoves
                    drawingPanel.getGraphics().setColor(new Color(255, 255, 0, 70));
                    drawingPanel.getGraphics().fillRect(x + 4, y + 4, cellSize - 8, cellSize - 8);
                }

                int[] pos = { i, j };

                if (isKingInCheck(Piece.Colors.black, this, board)
                        && pos[0] == (getPiece(Piece.Colors.black, Piece.PieceTypes.king).getPosition())[0]
                        && pos[1] == getPiece(Piece.Colors.black, Piece.PieceTypes.king).getPosition()[1]) {
                    if (isCheckmate(Piece.Colors.black, this, board)) { // checkmate
                        drawingPanel.getGraphics().setColor(new Color(50, 10, 90, 100)); // violet for checkmate
                    } else { // red for check
                        drawingPanel.getGraphics().setColor(new Color(250, 50, 50, 100));
                    }
                    drawingPanel.getGraphics().fillRect(x + 4, y + 4, cellSize - 8, cellSize - 8);
                }

                if (isKingInCheck(Piece.Colors.white, this, board)
                        && pos[0] == (getPiece(Piece.Colors.white, Piece.PieceTypes.king).getPosition())[0]
                        && pos[1] == getPiece(Piece.Colors.white, Piece.PieceTypes.king).getPosition()[1]) {
                    if (isCheckmate(Piece.Colors.white, this, board)) { // checkmate
                        drawingPanel.getGraphics().setColor(new Color(50, 10, 90, 100)); // violet for checkmate
                    } else { // red for check
                        drawingPanel.getGraphics().setColor(new Color(250, 50, 50, 100));
                    }
                    drawingPanel.getGraphics().fillRect(x + 4, y + 4, cellSize - 8, cellSize - 8);
                }

                Piece piece = board[i][j];
                if (piece != null) { // draws the piece if there is one on the square
                    char symbol = getSymbol(piece);
                    drawingPanel.getGraphics()
                            .setFont(drawingPanel.getGraphics().getFont().deriveFont((float) cellSize - 10));
                    int textWidth = drawingPanel.getGraphics().getFontMetrics().stringWidth(String.valueOf(symbol));
                    int textHeight = drawingPanel.getGraphics().getFontMetrics().getAscent();
                    int textX = x + (cellSize - textWidth) / 2;
                    int textY = y + (cellSize + textHeight) / 2 - 10;
                    drawingPanel.getGraphics().setColor(
                            (piece.getColor() == Piece.Colors.white) ? new Color(255, 255, 255) : new Color(0, 0, 0));
                    drawingPanel.getGraphics().drawString(String.valueOf(symbol), textX, textY);
                }
            }
        }
        String gameOverMessage = isGameOver();
        if (gameOverMessage != null) { // if the game is over, display the message and set gameOver to true to prevent
                                       // further moves
            gameOver = true;
            drawingPanel.getGraphics().setColor(new Color(255, 255, 255, 200));
            drawingPanel.getGraphics().fillRect(0, cellSize * 3, cellSize * 8, cellSize * 2);
            drawingPanel.getGraphics().setColor(new Color(0, 0, 0));
            drawingPanel.getGraphics().setFont(drawingPanel.getGraphics().getFont().deriveFont(24f));
            int textWidth = drawingPanel.getGraphics().getFontMetrics().stringWidth(gameOverMessage);
            drawingPanel.getGraphics().drawString(gameOverMessage, (cellSize * 8 - textWidth) / 2,
                    cellSize * 4 + cellSize / 2 - 40);
        }
    }

    public static char getSymbol(Piece piece) {
        return switch (piece.getType()) {
            case pawn -> piece.getColor() == Piece.Colors.white ? '♙' : '♟';
            case rook -> piece.getColor() == Piece.Colors.white ? '♖' : '♜';
            case knight -> piece.getColor() == Piece.Colors.white ? '♘' : '♞';
            case bishop -> piece.getColor() == Piece.Colors.white ? '♗' : '♝';
            case queen -> piece.getColor() == Piece.Colors.white ? '♕' : '♛';
            case king -> piece.getColor() == Piece.Colors.white ? '♔' : '♚';
        };
    }

    public boolean getIsGameOver() {
        return gameOver;
    }
}