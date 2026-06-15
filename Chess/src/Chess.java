import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.Random;

/* 
Name:  Callan Turner
Problem:  Created chess in java
Pseudocode:  get player 1 input, draw board, get player 2 input, draw board, repeat until game over
Notes:  More will be added to the game for bot
Maintenance log: 
Date:  
3/11/26 - Created chess board and pieces, added mouse listener for user input, added drawing panel for visual representation of the board
3/18/26 - Added piece movement and turn switching, castling, refactured abstract classpiece code and image loading.
3/24/26 - added en passant, added pawn promotion,
4/2/26 - Added check and checkmate detection, added move validation for pieces
*/ 
public class Chess {
    public static void main(String[] args) throws Exception {
        int boardSize = 600;
        int cellSize = boardSize / 8;

        Random random = new Random();

        DrawingPanel drawingPanel = new DrawingPanel(600, 600);

        drawingPanel.setBackground(Color.white);
        drawingPanel.setGridLines(true, cellSize);

        Board chessBoard = new Board(600, 600, drawingPanel, random.nextBoolean() ? Piece.Colors.white : Piece.Colors.black);

        MouseListener mouseListener = new MouseListener() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int y = e.getX() / cellSize;
                int x = e.getY() / cellSize;
                if (x < 0 || x > 7 || y < 0 || y > 7) {
                    return;
                }
                if (chessBoard.getPiece(x, y) == chessBoard.getSelectedPiece()) {
                    return;
                }
                if (chessBoard.getSelectedPiece() != null && chessBoard.getSelectedPiece() // click to move piece
                        .getColor() == (chessBoard.isWhiteTurn() ? Piece.Colors.white : Piece.Colors.black)) {
                    if (chessBoard.getPiece(x, y) == null 
                            || chessBoard.getPiece(x, y).getColor() != chessBoard.getSelectedPiece().getColor()) {
                        if (chessBoard.movePiece(chessBoard.getSelectedPiece(), x, y)) {
                            chessBoard.drawBoard(drawingPanel, null);
                            chessBoard.switchTurn();
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
            public void mousePressed(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
            }
        };

        drawingPanel.addMouseListener(mouseListener);

        chessBoard.resetBoard(drawingPanel);

        chessBoard.drawBoard(drawingPanel, null);

        while (true) {
            if (chessBoard.getSelectedPiece() != chessBoard.getLastPiece()) {
                chessBoard.drawBoard(drawingPanel, chessBoard.getSelectedPiece().getMoves(chessBoard, true));
            }
        }
    }
}
