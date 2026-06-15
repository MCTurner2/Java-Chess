import java.util.ArrayList;

public class BinaryBoard {
    public static boolean isInBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }

    public static ArrayList<int[]> getBinaryMovesForPiece(int x, int y, int[] boardBinary, Board chessBoard,
            int color) {
        ArrayList<int[]> moves = new ArrayList<>();
        int pieceBinary = getBinaryPiece(x, y, boardBinary);
        int pieceType = pieceBinary & 0b111; // 0b111 to get the last 3 bits for piece type
        int pieceColor = (pieceBinary >> 3) & 0b11; // shift right by 3 bits and mask to get the color
        if (pieceType != 0 && pieceColor == color) {
            if (pieceType == 1) { // pawn
                int direction = 0;
                if (pieceColor == 0) {
                    if (chessBoard.isWhiteIsOnBottom()) {
                        direction = -1;
                    } else {
                        direction = 1;
                    }
                } else {
                    if (chessBoard.isWhiteIsOnBottom()) {
                        direction = 1;
                    } else {
                        direction = -1;
                    }
                }
                int newRow = x + direction;
                if (isInBounds(newRow, y) && getBinaryPiece(newRow, y, boardBinary) == 0) { // normal move forward
                    moves.add(new int[] { x, y, newRow, y });
                    if ((direction == -1 && x == 6) || (direction == 1 && x == 1)) { // Check for double move
                        int doubleMoveRow = x + 2 * direction;
                        if (isInBounds(doubleMoveRow, y) && getBinaryPiece(doubleMoveRow, y, boardBinary) == 0) {
                            moves.add(new int[] { x, y, doubleMoveRow, y });
                        }
                    }
                }
                // check for en passant
                int enPassantRow = 0;
                if (pieceColor == 1 && chessBoard.isWhiteIsOnBottom()
                        || pieceColor == 0 && !chessBoard.isWhiteIsOnBottom()) {
                    enPassantRow = 3; // white can capture en passant on row 3
                } else {
                    enPassantRow = 4; // black can capture en passant on row 4
                }
                if (x == enPassantRow) {
                    int[] enPassantTarget = chessBoard
                            .getEnPassantTarget((pieceColor == 1) ? Piece.Colors.white : Piece.Colors.black);
                    if (enPassantTarget != null && enPassantTarget[0] == x && Math.abs(enPassantTarget[1] - y) == 1) {
                        if (isInBounds(x + direction, enPassantTarget[1])
                                && getBinaryPiece(x + direction, enPassantTarget[1], boardBinary) == 0) {                                                                                  
                            moves.add(new int[] { x, y, x + direction, enPassantTarget[1] });
                        }
                    }
                }
                
                for (int dy = -1; dy <= 1; dy += 2) { // Check for captures
                    int newCol = y + dy;
                    if (isInBounds(newRow, newCol)
                            && ((getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color)) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
            }
            if (pieceType == 2) { // rook
                int[][] rookDirections = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                for (int[] direction : rookDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 3) { // knight
                int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                        { 2, 1 } };
                for (int[] move : knightMoves) {
                    int newRow = x + move[0];
                    int newCol = y + move[1];
                    if (isInBounds(newRow, newCol)
                            && ((getBinaryPiece(newRow, newCol, boardBinary) & 0b111) == 0
                                    || ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color)) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
            }
            if (pieceType == 4) { // bishop
                int[][] bishopDirections = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
                for (int[] direction : bishopDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 5) { // queen
                int[][] queenDirections = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 },
                        { 1, -1 }, { 1, 1 } };
                for (int[] direction : queenDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 6) {// king
                int[][] kingMoves = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 },
                        { 1, 1 } };
                for (int[] move : kingMoves) {
                    int newRow = x + move[0];
                    int newCol = y + move[1];
                    if (!isInBounds(newRow, newCol)) {
                        continue;
                    }
                    int targetPiece = getBinaryPiece(newRow, newCol, boardBinary);
                    if (targetPiece == 0 || ((targetPiece >> 3) & 0b11) != color) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
                // Castling
                int castlingFlags = boardBinary[8] & 0xF;
                int opponentColor = (pieceColor == 0) ? 1 : 0;
                int kingRow = x;
                int kingCol = y;
                boolean isWhiteOnBottom = chessBoard.isWhiteIsOnBottom();
                // Kingside
                if (pieceColor == 0) { // white
                    if ((castlingFlags & 0b0001) != 0) { // white king-side
                        if (isInBounds(kingRow, kingCol + 1) && isInBounds(kingRow, kingCol + 2)
                                && getBinaryPiece(kingRow, kingCol + 1, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol + 2, boardBinary) == 0
                                && getBinaryPiece(kingRow, 7, boardBinary) != 0
                                && ((getBinaryPiece(kingRow, 7, boardBinary) & 0b111) == 4)
                                && !isSquareAttacked(kingRow, kingCol, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol + 1, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol + 2, opponentColor, boardBinary,
                                        isWhiteOnBottom)) {
                            moves.add(new int[] { x, y, kingRow, kingCol + 2 });
                        }
                    }
                    if ((castlingFlags & 0b0010) != 0) { // white queen-side
                        if (isInBounds(kingRow, kingCol - 1) && isInBounds(kingRow, kingCol - 2)
                                && isInBounds(kingRow, kingCol - 3)
                                && getBinaryPiece(kingRow, kingCol - 1, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol - 2, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol - 3, boardBinary) == 0
                                && getBinaryPiece(kingRow, 0, boardBinary) != 0
                                && ((getBinaryPiece(kingRow, 0, boardBinary) & 0b111) == 4)
                                && !isSquareAttacked(kingRow, kingCol, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol - 1, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol - 2, opponentColor, boardBinary,
                                        isWhiteOnBottom)) {
                            moves.add(new int[] { x, y, kingRow, kingCol - 2 });
                        }
                    }
                } else { // black
                    if ((castlingFlags & 0b0100) != 0) { // black king-side
                        if (isInBounds(kingRow, kingCol + 1) && isInBounds(kingRow, kingCol + 2)
                                && getBinaryPiece(kingRow, kingCol + 1, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol + 2, boardBinary) == 0
                                && getBinaryPiece(kingRow, 7, boardBinary) != 0
                                && ((getBinaryPiece(kingRow, 7, boardBinary) & 0b111) == 4)
                                && !isSquareAttacked(kingRow, kingCol, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol + 1, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol + 2, opponentColor, boardBinary,
                                        isWhiteOnBottom)) {
                            moves.add(new int[] { x, y, kingRow, kingCol + 2 });
                        }
                    }
                    if ((castlingFlags & 0b1000) != 0) { // black queen-side
                        if (isInBounds(kingRow, kingCol - 1) && isInBounds(kingRow, kingCol - 2)
                                && isInBounds(kingRow, kingCol - 3)
                                && getBinaryPiece(kingRow, kingCol - 1, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol - 2, boardBinary) == 0
                                && getBinaryPiece(kingRow, kingCol - 3, boardBinary) == 0
                                && getBinaryPiece(kingRow, 0, boardBinary) != 0
                                && ((getBinaryPiece(kingRow, 0, boardBinary) & 0b111) == 4)
                                && !isSquareAttacked(kingRow, kingCol, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol - 1, opponentColor, boardBinary, isWhiteOnBottom)
                                && !isSquareAttacked(kingRow, kingCol - 2, opponentColor, boardBinary,
                                        isWhiteOnBottom)) {
                            moves.add(new int[] { x, y, kingRow, kingCol - 2 });
                        }
                    }
                }
            }
        }
        ArrayList<int[]> finalMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i) == null) {
                continue; // skip if no valid move found
            }
            int fromRow = moves.get(i)[0];
            int fromCol = moves.get(i)[1];
            int fromPiece = getBinaryPiece(fromRow, fromCol, boardBinary);
            if (fromPiece == 0 || getBinaryColorFromPiece(fromPiece) != color) {
                continue; // skip if the move doesn't involve a piece of the bot's color
            }
            int[] move = moves.get(i);
            finalMoves.add(move);
        }
        return finalMoves; // returns move {fromRow, fromCol, toRow, toCol}
    }

    public static ArrayList<int[]> getBinaryMovesForPiece(int x, int y, int[] boardBinary,
            boolean isWhiteOnBottom, int[] enPassantTarget, int color) {
        ArrayList<int[]> moves = new ArrayList<>();
        int pieceBinary = getBinaryPiece(x, y, boardBinary);
        int pieceType = pieceBinary & 0b111;
        int pieceColor = (pieceBinary >> 3) & 0b11;
        if (pieceType != 0 && pieceColor == color) {
            if (pieceType == 1) { // pawn
                int direction = 0;
                if (pieceColor == 0) {
                    direction = isWhiteOnBottom ? -1 : 1;
                } else {
                    direction = isWhiteOnBottom ? 1 : -1;
                }
                int newRow = x + direction;
                if (isInBounds(newRow, y) && getBinaryPiece(newRow, y, boardBinary) == 0) {
                    moves.add(new int[] { x, y, newRow, y });
                    if (((direction == -1 && x == 6) || (direction == 1 && x == 1))
                            && getBinaryPiece(newRow, y, boardBinary) == 0
                            && getBinaryPiece(newRow + direction, y, boardBinary) == 0) {
                        int doubleMoveRow = x + 2 * direction;
                        if (isInBounds(doubleMoveRow, y) && getBinaryPiece(doubleMoveRow, y, boardBinary) == 0) {
                            moves.add(new int[] { x, y, doubleMoveRow, y });
                        }
                    }
                }
                // check for en passant
                int enPassantRow = 0;
                if (pieceColor == 1 && isWhiteOnBottom || pieceColor == 0 && !isWhiteOnBottom) {
                    enPassantRow = 3;
                } else {
                    enPassantRow = 4;
                }
                if (enPassantTarget != null && x == enPassantRow) {
                    if (enPassantTarget[0] == x && Math.abs(enPassantTarget[1] - y) == 1) {
                        if (isInBounds(x + direction, enPassantTarget[1])
                                && getBinaryPiece(x + direction, enPassantTarget[1], boardBinary) == 0) {
                            moves.add(new int[] { x, y, x + direction, enPassantTarget[1] });
                        }
                    }
                }
                for (int dy = -1; dy <= 1; dy += 2) { // Check for captures
                    int newCol = y + dy;
                    if (isInBounds(newRow, newCol)
                            && ((getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color)) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
            }
            if (pieceType == 2) { // rook
                int[][] rookDirections = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
                for (int[] direction : rookDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 3) { // knight
                int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                        { 2, 1 } };
                for (int[] move : knightMoves) {
                    int newRow = x + move[0];
                    int newCol = y + move[1];
                    if (isInBounds(newRow, newCol)
                            && ((getBinaryPiece(newRow, newCol, boardBinary) & 0b111) == 0
                                    || ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color)) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
            }
            if (pieceType == 4) { // bishop
                int[][] bishopDirections = { { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
                for (int[] direction : bishopDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 5) { // queen
                int[][] queenDirections = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 },
                        { 1, -1 }, { 1, 1 } };
                for (int[] direction : queenDirections) {
                    for (int i = 1; i < 8; i++) {
                        int newRow = x + direction[0] * i;
                        int newCol = y + direction[1] * i;
                        if (isInBounds(newRow, newCol) && getBinaryPiece(newRow, newCol, boardBinary) == 0) {
                            moves.add(new int[] { x, y, newRow, newCol });
                        } else {
                            if (isInBounds(newRow, newCol)
                                    && (getBinaryPiece(newRow, newCol, boardBinary) & 0b111) != 0
                                    && ((getBinaryPiece(newRow, newCol, boardBinary) >> 3) & 0b11) != color) {
                                moves.add(new int[] { x, y, newRow, newCol });
                                break;
                            }
                            break;
                        }
                    }
                }
            }
            if (pieceType == 6) {// king
                int[][] kingMoves = { { -1, -1 }, { -1, 0 }, { -1, 1 }, { 0, -1 }, { 0, 1 }, { 1, -1 }, { 1, 0 },
                        { 1, 1 } };
                for (int[] move : kingMoves) {
                    int newRow = x + move[0];
                    int newCol = y + move[1];
                    if (!isInBounds(newRow, newCol)) {
                        continue;
                    }
                    int targetPiece = getBinaryPiece(newRow, newCol, boardBinary);
                    if (targetPiece == 0 || ((targetPiece >> 3) & 0b11) != color) {
                        moves.add(new int[] { x, y, newRow, newCol });
                    }
                }
            }
        }
        ArrayList<int[]> finalMoves = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            if (moves.get(i) == null) {
                continue; // skip if no valid move found
            }
            int fromRow = moves.get(i)[0];
            int fromCol = moves.get(i)[1];
            int fromPiece = getBinaryPiece(fromRow, fromCol, boardBinary);
            if (fromPiece == 0 || getBinaryColorFromPiece(fromPiece) != color) {
                continue; // skip if the move doesn't involve a piece of the bot's color
            }
            int[] move = moves.get(i);
            finalMoves.add(move);
        }
        return finalMoves; // returns move {fromRow, fromCol, toRow, toCol}
    }

    public static boolean isKingInCheck(int[] boardBinary, int color, boolean isWhiteOnBottom) {
        int[] kingPosition = getKingPosition(color, boardBinary);
        if (kingPosition == null) {
            return true; // king not found
        }
        int kingRow = kingPosition[0]; 
        int kingCol = kingPosition[1];

        int opponentColor = (color == 0) ? 1 : 0;
        int pawnDirection = 0;
        if (opponentColor == 0) {
            pawnDirection = isWhiteOnBottom ? -1 : 1;
        } else {
            pawnDirection = isWhiteOnBottom ? 1 : -1;
        }
        int pawnRow = kingRow + pawnDirection;
        for (int dc = -1; dc <= 1; dc += 2) { // check for opponent pawns diagonally in front of the king
            int pawnCol = kingCol + dc;
            if (isInBounds(pawnRow, pawnCol)) {
                int pieceBinary = getBinaryPiece(pawnRow, pawnCol, boardBinary);
                if ((pieceBinary & 0b111) == 1 && ((pieceBinary >> 3) & 0b11) == opponentColor) {
                    return true;
                }
            }
        }
        // Check for knights
        int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                { 2, 1 } };
        for (int[] move : knightMoves) {
            int newRow = kingRow + move[0];
            int newCol = kingCol + move[1];
            if (isInBounds(newRow, newCol)) {
                int pieceBinary = getBinaryPiece(newRow, newCol, boardBinary);
                if ((pieceBinary & 0b111) == 3 && ((pieceBinary >> 3) & 0b11) == opponentColor) {
                    return true;
                }
            }
        }
        // Check for rooks bishops and queens
        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        for (int d = 0; d < directions.length; d++) {
            int dr = directions[d][0];
            int dc = directions[d][1];
            int newRow = kingRow + dr;
            int newCol = kingCol + dc;
            while (isInBounds(newRow, newCol)) {
                int pieceBinary = getBinaryPiece(newRow, newCol, boardBinary);
                int pType = pieceBinary & 0b111;
                int pColor = (pieceBinary >> 3) & 0b11;
                if (pType != 0) {
                    if (pColor == opponentColor) {
                        if ((d < 4 && (pType == 2 || pType == 5)) || (d >= 4 && (pType == 4 || pType == 5))) {
                            // rook or queen in orthogonal direction, bishop or queen in diagonal direction
                            return true;
                        }
                    }
                    break;
                }
                newRow += dr;
                newCol += dc;
            }
        }
        return false;
    }

    public static int getPieceTypeFromBinary(int pieceBinary) {
        int pieceType = pieceBinary & 0b111; // last 3 bits for piece type
        int color = (pieceBinary >> 3) & 0b11; // next 2 bits for color, 0 for white, 1 for black
        switch (pieceType) {
            case 0:
                return 0; // empty
            case 1: // pawn
                return (color == 0) ? 1 : -1;
            case 2: // knight
                return (color == 0) ? 2 : -2;
            case 3: // bishop
                return (color == 0) ? 3 : -3;
            case 4: // rook
                return (color == 0) ? 4 : -4;
            case 5: // queen
                return (color == 0) ? 5 : -5;
            case 6: // king
                return (color == 0) ? 6 : -6;
            default:
                return 0;
        }
    }

    public static boolean isKingInCheck(int[] boardBinary, int color, boolean isWhiteOnBottom, int[] enPassantTarget) {
        int[] kingPosition = getKingPosition(color, boardBinary);
        ArrayList<int[]> opponentMoves = new ArrayList<>();
        int opponentColor = (color == 0) ? 1 : 0;
        for (int i = 0; i < 8; i++) { // loop through the board to find opponent pieces and their moves
            for (int j = 0; j < 8; j++) {
                int pieceBinary = getBinaryPiece(i, j, boardBinary);
                if ((pieceBinary & 0b111) != 0 && ((pieceBinary >> 3) & 0b11) != color) {
                    opponentMoves
                            .addAll(getBinaryMovesForPiece(i, j, boardBinary, isWhiteOnBottom, enPassantTarget,
                                    opponentColor));
                }
            }
        }
        for (int[] move : opponentMoves) {
            if (move[2] == kingPosition[0] && move[3] == kingPosition[1]) {
                return true;
            }
        }
        return false;
    }

    public static Board convertBinaryToBoard(int[] boardBinary, DrawingPanel drawingPanel) {
        Board chessBoard = new Board(600, 600, drawingPanel,
                (boardBinary[0] & 0b1111) == 0 ? Piece.Colors.white : Piece.Colors.black);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int pieceBinary = getBinaryPiece(i, j, boardBinary);
                int pieceType = pieceBinary & 0b111; // get piece type bits
                int color = (pieceBinary >> 3) & 0b11; // get color bits
                if (pieceType != 0) {
                    Piece piece = null;
                    switch (pieceType) {
                        case 1:
                            piece = new Pawn((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                        case 2:
                            piece = new Knight((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                        case 3:
                            piece = new Bishop((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                        case 4:
                            piece = new Rook((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                        case 5:
                            piece = new Queen((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                        case 6:
                            piece = new King((color == 0) ? Piece.Colors.white : Piece.Colors.black, i, j);
                            break;
                    }
                    if (piece != null) {
                        chessBoard.addPiece(piece);
                    }
                }
            }
        }
        return chessBoard;
    }

    public static int[] getKingPosition(int kingColor, int[] board) { // find king position
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, board);
                if ((pieceBinary & 0b111) == 6 && ((pieceBinary >> 3) & 0b11) == kingColor) {
                    return new int[] { i, j };
                }
            }
        }
        return null;
    }

    public static int getBinaryColorFromPiece(int pieceBinary) {
        return (pieceBinary >> 3) & 0b11; // extract the color bits
    }

    public static int getBinaryPiece(int x, int y, int[] boardBinary) {
        return (boardBinary[x] >> ((7 - y) * 4)) & 0xf; // extract the 4 bits for the piece at (x, y)
    }

    public static int getPieceBinary(int pieceType, int color) {
        return (color << 3) | pieceType; // combine color and piece type into a single integer
    }

    public static int[] setBinaryPiece(int x, int y, int pieceBinary, int[] boardBinary) {
        boardBinary[x] &= ~(0b1111 << ((7 - y) * 4)); // clear the bits for the piece at (x, y)
        boardBinary[x] |= (pieceBinary & 0b1111) << ((7 - y) * 4); // set the bits for the new piece
        return boardBinary;
    }

    public static int[] getEnPassantTargetFromBinary(int[] boardBinary) {
        return new int[] { (boardBinary[8] >> 4) & 0xF, (boardBinary[8] >> 8) & 0xF };
    }

    public static int[] makeBinaryMove(int fromX, int fromY, int toX, int toY, int[] boardBinary,
            boolean isWhiteOnBottom) {
        int pieceBinary = getBinaryPiece(fromX, fromY, boardBinary);
        int pieceType = pieceBinary & 0b111; // get piece type bits
        int pieceColor = (pieceBinary >> 3) & 0b11; // get color bits
        int[] newBoardBinary = setBinaryPiece(fromX, fromY, 0, boardBinary); // clear the from square
        newBoardBinary = setBinaryPiece(toX, toY, pieceBinary, newBoardBinary); // set the to square
        int castlingRights = boardBinary[8] & 0xF; // get existing castling rights from the 9th int

        if (pieceType == 6) { // king moved, lose castling rights for this color
            if (pieceColor == 0) {
                castlingRights &= ~0b0011; // clear bits 0 and 1 for white castling rights
            } else {
                castlingRights &= ~0b1100; // clear bits 2 and 3 for black castling rights
            }
            if (Math.abs(toY - fromY) == 2) { // castling move, also move the rook
                int direction = (toY > fromY) ? 1 : -1;
                int rookCol = -1;   
                for (int c = fromY + direction; c >= 0 && c < 8; c += direction) { // find rook in the direction of castling
                    int piece = getBinaryPiece(fromX, c, boardBinary);
                    if (piece != 0 && (piece & 0b111) == 2 && ((piece >> 3) & 0b11) == pieceColor) {
                        rookCol = c;
                        break;
                    }
                }
                if (rookCol != -1) {
                    int rookDestCol = toY - direction; // rook moves to square adjacent to king destination
                    newBoardBinary = setBinaryPiece(fromX, rookDestCol, getBinaryPiece(fromX, rookCol, boardBinary),
                            newBoardBinary);
                    newBoardBinary = setBinaryPiece(fromX, rookCol, 0, newBoardBinary);
                }
            }
        } else if (pieceType == 2) { // rook moved, lose appropriate castling right if rook started from home square
            int whiteHomeRow = isWhiteOnBottom ? 7 : 0;
            int blackHomeRow = isWhiteOnBottom ? 0 : 7;
            if (pieceColor == 0) { // white rook
                if (fromX == whiteHomeRow && fromY == 0) {
                    castlingRights &= ~0b0010; // white queenside
                } else if (fromX == whiteHomeRow && fromY == 7) {
                    castlingRights &= ~0b0001; // white kingside
                }
            } else { // black rook
                if (fromX == blackHomeRow && fromY == 0) {
                    castlingRights &= ~0b1000; // black queenside
                } else if (fromX == blackHomeRow && fromY == 7) {
                    castlingRights &= ~0b0100; // black kingside
                }
            }
        }

        int enPassantRow = (boardBinary[8] >> 4) & 0xF; // get en passant target row from the 9th int
        int enPassantCol = (boardBinary[8] >> 8) & 0xF; // get en passant target column from the 9th int
        if (pieceType == 1 && Math.abs(toY - fromY) == 1 && getBinaryPiece(toX, toY, boardBinary) == 0
                && enPassantRow == toX && enPassantCol == toY) { // en passant capture
            newBoardBinary = setBinaryPiece(fromX, toY, 0, newBoardBinary);
        }

        newBoardBinary[8] = castlingRights; // update castling rights in the 9th int
        if (pieceType == 1 && Math.abs(toX - fromX) == 2 && fromY == toY) { // Set en passant target if this move is a
                                                                            // two square pawn advance.
            int epRow = (fromX + toX) / 2;
            int epCol = toY;
            newBoardBinary[8] |= (epRow << 4) | (epCol << 8); // store en passant target in the 9th int
        }
        return newBoardBinary;
    }

    public static boolean isCheckmate(int[] boardBinary, int color) { // check if the current player is in checkmate
        Board simulatedBoard = convertBinaryToBoard(boardBinary, null);
        return simulatedBoard.isCheckmate((color == 0) ? Piece.Colors.white : Piece.Colors.black, simulatedBoard,
                simulatedBoard.getPieces());
    }

    public static int[] getBoardBinaryArray(Board chessBoard) {
        // convert the current board into a binary for quicker move analysis and move generation
        // the board is stored as an array of 9 ints - 8 for the pieces and 1 for
        // additional info (castling rights, en passant)
        int[] boardBinary = new int[9];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = chessBoard.getPiece(i, j);
                int pieceType = (piece == null) ? 0 : piece.getType().ordinal() + 1;
                int color = (piece == null) ? 0 : (piece.getColor() == Piece.Colors.white) ? 0 : 1;
                boardBinary[i] <<= 4; // shift left by 4 bits to make room for the next piece
                boardBinary[i] |= getPieceBinary(pieceType, color); // add the piece's binary representation
            }
        }

        boardBinary[8] = 0; 
        if (chessBoard.canCastleKingSide(Piece.Colors.white)) {
            boardBinary[8] |= 0b0001; // set bit 0 for white king-side castling right
        }
        if (chessBoard.canCastleQueenSide(Piece.Colors.white)) {
            boardBinary[8] |= 0b0010; // set bit 1 for white queen-side castling right
        }
        if (chessBoard.canCastleKingSide(Piece.Colors.black)) {
            boardBinary[8] |= 0b0100; // set bit 2 for black king-side castling right
        }
        if (chessBoard.canCastleQueenSide(Piece.Colors.black)) {
            boardBinary[8] |= 0b1000; // set bit 3 for black queen-side castling right
        }
        Piece.Colors enPassantColor = chessBoard.isWhiteTurn() ? Piece.Colors.white : Piece.Colors.black;
        int[] enPassantTarget = chessBoard.getEnPassantTarget(enPassantColor);
        if (enPassantTarget != null) {
            boardBinary[8] |= (enPassantTarget[0] << 4) | (enPassantTarget[1] << 8); // store en passant target in the 9th int
        }
        return boardBinary;
    }

    public static boolean isSquareAttacked(int targetRow, int targetCol, int attackerColor, int[] boardBinary,
            boolean isWhiteOnBottom) { // check if the target square is attacked by any piece of the attacker color
        int pawnDirection = (attackerColor == 0) ? (isWhiteOnBottom ? -1 : 1) : (isWhiteOnBottom ? 1 : -1);
        int pawnRow = targetRow + pawnDirection;
        for (int i = -1; i <= 1; i += 2) { // check for opponent pawns diagonally in front of the target square
            int pawnCol = targetCol + i;
            if (BinaryBoard.isInBounds(pawnRow, pawnCol)) {
                int pieceBinary = BinaryBoard.getBinaryPiece(pawnRow, pawnCol, boardBinary);
                if ((pieceBinary & 0b111) == 1 && ((pieceBinary >> 3) & 0b11) == attackerColor) {
                    return true;
                }
            }
        }
        // Check for knights
        int[][] knightMoves = { { -2, -1 }, { -2, 1 }, { -1, -2 }, { -1, 2 }, { 1, -2 }, { 1, 2 }, { 2, -1 },
                { 2, 1 } };
        for (int[] move : knightMoves) {
            int newRow = targetRow + move[0];
            int newCol = targetCol + move[1];
            if (BinaryBoard.isInBounds(newRow, newCol)) {
                int pieceBinary = BinaryBoard.getBinaryPiece(newRow, newCol, boardBinary);
                if ((pieceBinary & 0b111) == 3 && ((pieceBinary >> 3) & 0b11) == attackerColor) {
                    return true;
                }
            }
        }
        // Check for rooks bishops and queens
        int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 }, { -1, -1 }, { -1, 1 }, { 1, -1 }, { 1, 1 } };
        for (int d = 0; d < directions.length; d++) {
            int row = directions[d][0];
            int col = directions[d][1];
            int newRow = targetRow + row;
            int newCol = targetCol + col;
            while (BinaryBoard.isInBounds(newRow, newCol)) {
                int pieceBinary = BinaryBoard.getBinaryPiece(newRow, newCol, boardBinary);
                int pType = pieceBinary & 0b111;
                int pColor = (pieceBinary >> 3) & 0b11;
                if (pType != 0) {
                    if (pColor == attackerColor) {
                        if (pType == 6 && Math.abs(newRow - targetRow) <= 1 && Math.abs(newCol - targetCol) <= 1) {
                            return true;
                        }
                        if ((d < 4) && (pType == 2 || pType == 5)) {
                            return true;
                        }
                        if ((d >= 4) && (pType == 4 || pType == 5)) {
                            return true;
                        }
                    }
                    break;
                }
                newRow += row;
                newCol += col;
            }
        }
        return false;
    }

    public static int getPieceValue(int pieceBinary) {
        int pieceType = pieceBinary & 0b111; // get piece type bits
        switch (pieceType) {
            case 1: // pawn
                return 1;
            case 2: // rook
                return 5;
            case 3: // knight
                return 3;
            case 4: // bishop
                return 3;
            case 5: // queen
                return 9;
            case 6: // king
                return 150; // prioritize checkmate
            default:
                return 0;
        }
    }
}
