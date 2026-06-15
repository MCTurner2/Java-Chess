import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.Random;

public class OnePlayerChess {
    public static void main(String[] args) throws Exception {
        int boardSize = 750;
        int cellSize = boardSize / 8;

        Random random = new Random();

        Boolean whiteIsBottom = random.nextBoolean();

        DrawingPanel drawingPanel = new DrawingPanel(boardSize, boardSize);

        drawingPanel.setBackground(Color.white);
        drawingPanel.setGridLines(true, cellSize);

        Board chessBoard = new Board(boardSize, boardSize, drawingPanel, whiteIsBottom ? Piece.Colors.white : Piece.Colors.black);
        Bot bot = new Bot(5, whiteIsBottom ? Piece.Colors.black : Piece.Colors.white, drawingPanel, chessBoard.isWhiteIsOnBottom());
        Bot opponentBot = new Bot(5, bot.getColor() == Piece.Colors.white ? Piece.Colors.black : Piece.Colors.white, drawingPanel, chessBoard.isWhiteIsOnBottom());

        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (chessBoard.getIsGameOver()) {
                    return; // Ignore clicks if the game is over
                }
                if (chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.white || 
                        !chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.black) {
                    return; // Bot's turn, ignore user input
                }
                int y = e.getX() / cellSize;
                int x = e.getY() / cellSize;
                if (x < 0 || x > 7 || y < 0 || y > 7) {
                    return;
                }
                if (chessBoard.getPiece(x, y) == chessBoard.getSelectedPiece()) {
                    return;
                }
                if (chessBoard.getSelectedPiece() != null && chessBoard.getSelectedPiece()
                        .getColor() == (chessBoard.isWhiteTurn() ? Piece.Colors.white : Piece.Colors.black)) {
                    if (chessBoard.getPiece(x, y) == null 
                            || chessBoard.getPiece(x, y).getColor() != chessBoard.getSelectedPiece().getColor()) {
                        chessBoard.drawBoard(drawingPanel, null);
                        if (chessBoard.movePiece(chessBoard.getSelectedPiece(), x, y)) {
                            chessBoard.switchTurn();
                            chessBoard.drawBoard(drawingPanel, null);
                            // After human move, let the bot move if it's now the bot's turn
                            if (chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.white ||
                                !chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.black) {
                                bot.makeMove(chessBoard);
                                chessBoard.drawBoard(drawingPanel, null);
                            }
                        }
                        chessBoard.setSelectedPiece(null);
                        return;
                    }
                }
                if (chessBoard.getPiece(x, y) == null) {
                    return;
                } else if ((chessBoard.isWhiteTurn() && chessBoard.getPiece(x, y).getColor() == Piece.Colors.black) ||
                        (!chessBoard.isWhiteTurn() && chessBoard.getPiece(x, y).getColor() == Piece.Colors.white)) {
                    return; 
                }
                chessBoard.setSelectedPiece(chessBoard.getPiece(x, y));
                chessBoard.drawBoard(drawingPanel, chessBoard.getSelectedPiece().getMoves(chessBoard, true));
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {}
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {}
        };
        drawingPanel.addMouseListener(mouseListener);

        chessBoard.resetBoard(drawingPanel);

        chessBoard.drawBoard(drawingPanel, null);
        // If the bot goes first, let it make the opening move
        if (chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.white ||
            !chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.black) {
            bot.makeMove(chessBoard);
            chessBoard.drawBoard(drawingPanel, null);
        }

        // while (true) { // bots compete, comment out if you want to play against the bot
        //     if (chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.white ||
        //         !chessBoard.isWhiteTurn() && bot.getColor() == Piece.Colors.black) {
        //         bot.makeMove(chessBoard);
        //     } else {
        //         opponentBot.makeMove(chessBoard);
        //     }
        //     chessBoard.drawBoard(drawingPanel, null);
        //     Thread.sleep(200);
        //     if (chessBoard.getIsGameOver()) {
        //         break; 
        //     }
        // }
    }
}