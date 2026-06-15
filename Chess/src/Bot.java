import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.time.Instant;

public class Bot {
    private int depth;
    private int color;
    DrawingPanel drawingPanel;
    boolean isWhiteOnBottom;
    private int boardsEvaluated = 0;
    private static final int MAX_POSITION_CACHE_SIZE = 50000;
    private final Map<String, ArrayList<int[]>> positionCache = new LinkedHashMap<String, ArrayList<int[]>>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ArrayList<int[]>> eldest) { // store moves for previously seen positions to speed up move generation
            return size() > MAX_POSITION_CACHE_SIZE; // limit cache size to prevent memory issues
        }
    };

    Bot(int depth, Piece.Colors color, DrawingPanel drawingPanel, boolean isWhiteOnBottom) {
        this.depth = depth; // how many moves ahead the bot should look when evaluating moves
        this.color = (color == Piece.Colors.white) ? 0 : 1; // 0 for white, 1 for black (match binary encoding)
        this.drawingPanel = drawingPanel;
        this.isWhiteOnBottom = isWhiteOnBottom; // Set the initial value based on your game setup
    }

    public Piece.Colors getColor() {
        return (color == 0) ? Piece.Colors.white : Piece.Colors.black;
    }

    public int getDepth() {
        return depth;
    }

    public void makeMove(Board chessBoard) {
        Instant startTime = Instant.now();
        System.out.println("Bot is making a move. Current color: " + color);
        int[] boardBinary = BinaryBoard.getBoardBinaryArray(chessBoard); // get current board state in binary form for quick processing
        int[] bestMove = getBestMoveLookAhead(boardBinary, chessBoard); // returns an array of {fromX, fromY, toX, toY}
        if (bestMove == null) {
            System.out.println("No valid moves found for bot.");
            return;
        }
        int fromX = bestMove[0];
        int fromY = bestMove[1];
        int newX = bestMove[2];
        int newY = bestMove[3];
        Piece pieceToMove = chessBoard.getPiece(fromX, fromY);
        Instant endTime = Instant.now();
        System.out.println("Boards evaluated in this move: " + boardsEvaluated);
        boardsEvaluated = 0; // reset for next move

        System.out.println("Selected move: from (" + fromX + ", " + fromY + ") to (" + newX + ", " + newY + ")");
        if (pieceToMove == null) {
            System.out.println("No piece found at from-coordinates");
            System.out.println("Time taken: " + (endTime.toEpochMilli() - startTime.toEpochMilli()) + " ms");
            return;
        }
        System.out.println("Piece at from-coordinates: " + pieceToMove.getType() + " " + pieceToMove.getColor());
        boolean moved = chessBoard.movePiece(pieceToMove, newX, newY); // attempt to make the move on the actual board
        System.out.println("Move executed: " + moved);
        if (!moved) {
            System.out.println("Move was not made. Piece: " + pieceToMove.getType() + " " + pieceToMove.getColor()
                    + " from (" + fromX + ", " + fromY + ") to (" + newX + ", " + newY + ")");
            ArrayList<int[]> fallbackMoves = getAllPossibleMoves(boardBinary, color);
            orderMoves(boardBinary, fallbackMoves);
            for (int[] move : fallbackMoves) {
                if (move[0] == fromX && move[1] == fromY && move[2] == newX && move[3] == newY) {
                    continue;
                }
                Piece fallbackPiece = chessBoard.getPiece(move[0], move[1]);
                if (fallbackPiece != null && chessBoard.movePiece(fallbackPiece, move[2], move[3])) {
                    System.out.println("Fallback move made: from (" + move[0] + ", " + move[1] + ") to (" + move[2]
                            + ", " + move[3] + ")");
                    chessBoard.switchTurn();
                    System.out.println("Time taken: " + (endTime.toEpochMilli() - startTime.toEpochMilli()) + " ms");
                    return;
                }
            }
        } else {
            chessBoard.switchTurn();
            System.out.println("Time taken: " + (endTime.toEpochMilli() - startTime.toEpochMilli()) + " ms");
        }
    }

    private int[] getBestMoveLookAhead(int[] boardBinary, Board chessBoard) {
        ArrayList<int[]> possibleMoves = getAllPossibleMoves(boardBinary, color); // get all legal moves for the bot's color in the current position
        if (possibleMoves.isEmpty()) {
            System.out.println("No valid moves found for bot.");
            return null;
        }

        int bestRating = Integer.MIN_VALUE; // initialize best rating to worst possible value so any valid move will be better
        int[] bestMove = null;
        int opponentColor = (color == 0) ? 1 : 0;
        Map<String, Integer> repetitionCounts = buildRepetitionMap(chessBoard); // build a map of how many times each position has occurred in the game so far to handle threefold repetition

        orderMoves(boardBinary, possibleMoves);
        int[] immediateCapture = findImmediateUndefendedCapture(boardBinary, possibleMoves);
        if (immediateCapture != null) {
            System.out.println("Found undefended capture: from (" + immediateCapture[0] + ", " + immediateCapture[1] + ") to (" + immediateCapture[2] + ", " + immediateCapture[3] + ")");
            return immediateCapture;
        }
        boolean isWinning = evaluateBoard(boardBinary) > 50;
        int[] fallbackMove = null; // 
        for (int[] move : possibleMoves) {  // for each possible move, simulate it and evaluate the resulting position
            int[] newBoardBinary = BinaryBoard.makeBinaryMove(move[0], move[1], move[2], move[3], boardBinary.clone(), isWhiteOnBottom);
            String position = Arrays.toString(newBoardBinary);
            int originalCount = repetitionCounts.getOrDefault(position, 0); // check how many times the resulting position has occurred before
            repetitionCounts.put(position, originalCount + 1);

            int score = Integer.MIN_VALUE;
            boolean forcedDrawOrStalemate = false;
            if (isWinning && originalCount >= 2) {
                score = -50000; // avoid moving into a position that would be the third occurrence when ahead
                forcedDrawOrStalemate = true;
                continue; // skip this repeated move, but keep evaluating the others
            } else {
                ArrayList<int[]> oppMovesAfter = getAllPossibleMoves(newBoardBinary, opponentColor);
                boolean oppInCheck = BinaryBoard.isKingInCheck(newBoardBinary, opponentColor, isWhiteOnBottom);
                if (isWinning && oppMovesAfter.isEmpty() && !oppInCheck) { // stalemate would occur
                    score = -100000; // extremely bad when winning
                    forcedDrawOrStalemate = true;
                    if (fallbackMove == null) {
                        fallbackMove = move;
                    }
                }
            }
            if (!forcedDrawOrStalemate) {
                score = getBestMoveValue(newBoardBinary, depth - 1, opponentColor,
                        Integer.MIN_VALUE, Integer.MAX_VALUE, repetitionCounts);
            }
            if (originalCount == 0) {
                repetitionCounts.remove(position);
            } else {
                repetitionCounts.put(position, originalCount);
            }

            if (score > bestRating) { 
                bestRating = score;
                bestMove = move;
            }
        }
        if (bestMove == null && fallbackMove != null) {
            bestMove = fallbackMove;
        }

        if (bestMove == null) {
            System.out.println("No valid moves found for bot");
            return null;
        }
        System.out.println("Best move selected: from (" + bestMove[0] + ", " + bestMove[1] + ") to ("
                + bestMove[2] + ", " + bestMove[3] + ") with rating: " + bestRating);
        return bestMove;
    }

    private int getBestMoveValue(int[] boardBinary, int currentDepth, int currentColor, int lowerBound, int upperBound,
            Map<String, Integer> repetitionCounts) {
        ArrayList<int[]> moves = getAllPossibleMoves(boardBinary, currentColor);
        boolean inCheck = BinaryBoard.isKingInCheck(boardBinary, currentColor, isWhiteOnBottom);
        if (moves.isEmpty()) {
            if (inCheck) {
                return (currentColor == color) ? -10000000 : 10000000;
            }
            return (currentColor == color) ? -5000000 : 0;
        }
        if (currentDepth == 0) {
            return evaluateBoard(boardBinary);
        }

        orderMoves(boardBinary, moves);
        if (currentColor == color) { // players turn - maximize score
            int value = Integer.MIN_VALUE;
            int opponentColor = (currentColor == 0) ? 1 : 0;
            for (int[] move : moves) {
                int[] newBoardBinary = BinaryBoard.makeBinaryMove(move[0], move[1], move[2], move[3],
                    boardBinary.clone(), isWhiteOnBottom);
                String positionKey = Arrays.toString(newBoardBinary);
                int originalCount = repetitionCounts.getOrDefault(positionKey, 0);
                if (originalCount >= 2) {
                    value = Math.max(value, -50000); // avoid a position that would become the third occurrence
                } else {
                    repetitionCounts.put(positionKey, originalCount + 1);
                    int score = getBestMoveValue(newBoardBinary, currentDepth - 1, opponentColor, lowerBound,
                            upperBound,
                            repetitionCounts);
                    if (originalCount == 0) {
                        repetitionCounts.remove(positionKey);
                    } else {
                        repetitionCounts.put(positionKey, originalCount);
                    }
                    value = Math.max(value, score);
                }
                boardsEvaluated++;
                lowerBound = Math.max(lowerBound, value); // update lower bound for pruning
                if (lowerBound >= upperBound) {
                    break;
                }
            }
            return value;
        } else { // opponent's turn, minimize score
            int value = Integer.MAX_VALUE;
            int opponentColor = (currentColor == 0) ? 1 : 0;
            for (int[] move : moves) {
                int[] newBoardBinary = BinaryBoard.makeBinaryMove(move[0], move[1], move[2], move[3],
                    boardBinary.clone(), isWhiteOnBottom);
                String positionKey = Arrays.toString(newBoardBinary);
                int originalCount = repetitionCounts.getOrDefault(positionKey, 0);
                if (originalCount >= 2) {
                    value = Math.min(value, 0); // opponent can force a draw by making it the third occurrence
                } else {
                    repetitionCounts.put(positionKey, originalCount + 1);
                    int score = getBestMoveValue(newBoardBinary, currentDepth - 1, opponentColor, lowerBound,
                            upperBound,
                            repetitionCounts);
                    if (originalCount == 0) {
                        repetitionCounts.remove(positionKey);
                    } else {
                        repetitionCounts.put(positionKey, originalCount);
                    }
                    value = Math.min(value, score);
                }
                boardsEvaluated++;
                upperBound = Math.min(upperBound, value); // update upper bound for pruning
                if (lowerBound >= upperBound) {
                    break;
                }
            }
            return value;
        }
    }

    private String buildBoardKey(int[] boardBinary) {
        return Arrays.toString(boardBinary);
    }

    private ArrayList<int[]> getCachedMoves(int[] boardBinary, int color) {
        ArrayList<int[]> cachedMoves = positionCache.get(buildBoardKey(boardBinary) + "|" + color);
        return (cachedMoves == null) ? null : new ArrayList<>(cachedMoves);
    }

    private void cacheMoves(int[] boardBinary, int color, ArrayList<int[]> moves) {
        positionCache.put(buildBoardKey(boardBinary) + "|" + color, new ArrayList<>(moves));
    }

    private Map<String, Integer> buildRepetitionMap(Board chessBoard) {
        Map<String, Integer> repetitionCounts = new HashMap<>();
        for (String position : chessBoard.getRepetitionHistory()) { // build a map of how many times each position has occurred in the game so far to handle threefold repetition
            repetitionCounts.put(position, repetitionCounts.getOrDefault(position, 0) + 1);
        }
        return repetitionCounts;
    }

    private ArrayList<int[]> getAllPossibleMoves(int[] boardBinary, int color) {
        ArrayList<int[]> cachedMoves = getCachedMoves(boardBinary, color);
        if (cachedMoves != null) {
            return cachedMoves;
        }

        ArrayList<int[]> moves = new ArrayList<>();
        int[] enPassantTarget = null;
        int epRow = (boardBinary[8] >> 4) & 0xF; // get en passant target row
        int epCol = (boardBinary[8] >> 8) & 0xF; // get en passant target column
        if (epRow != 0 || epCol != 0) {  // if there is an en passant target, decode its coordinates from the binary board representation
            enPassantTarget = new int[] { epRow, epCol };
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, boardBinary);
                int pieceType = pieceBinary & 0b111;
                int pieceColor = (pieceBinary >> 3) & 0b11;
                if (pieceType != 0 && pieceColor == color) {
                    ArrayList<int[]> possibleMoves = BinaryBoard.getBinaryMovesForPiece(i, j, boardBinary,
                            isWhiteOnBottom, enPassantTarget, color);
                    for (int[] move : possibleMoves) { // filter out moves that would leave our king in check
                        if (isLegalBinaryMove(boardBinary, move, color)) {
                            moves.add(move);
                        }
                    }
                }
            }
        }
        cacheMoves(boardBinary, color, moves);
        return moves;
    }

    private void orderMoves(int[] boardBinary, ArrayList<int[]> moves) {
        moves.sort((a, b) -> { // order moves by capture value and center control to improve pruning efficiency
            int aValue = getMoveOrderValue(boardBinary, a);
            int bValue = getMoveOrderValue(boardBinary, b);
            return Integer.compare(bValue, aValue);
        });
    }

    private boolean isLegalBinaryMove(int[] boardBinary, int[] move, int color) {
                int[] newBoardBinary = BinaryBoard.makeBinaryMove(move[0], move[1], move[2], move[3], boardBinary.clone(), isWhiteOnBottom);
        return !BinaryBoard.isKingInCheck(newBoardBinary, color, isWhiteOnBottom); // move is legal if it does not leave our king in check
    }

    private int getMoveOrderValue(int[] boardBinary, int[] move) { // assign a value to the move based on whether it captures an opponent piece and whether it moves towards the center
        int target = BinaryBoard.getBinaryPiece(move[2], move[3], boardBinary);
        int captureValue = BinaryBoard.getPieceValue(target);
        if (captureValue != 0) {
            return captureValue * 1000;
        }
        int column = move[3];
        int centerBonus = (column >= 2 && column <= 5) ? 10 : 0;
        return centerBonus;
    }

    private int[] findImmediateUndefendedCapture(int[] boardBinary, ArrayList<int[]> moves) {
        int[] bestCapture = null;
        int bestCaptureValue = 0;
        for (int[] move : moves) {
            int target = BinaryBoard.getBinaryPiece(move[2], move[3], boardBinary);
            if (target == 0) {
                continue;
            }
            int targetColor = (target >> 3) & 0b11; // get target piece color
            int pieceValue = BinaryBoard.getPieceValue(target);
            if (targetColor != color && !BinaryBoard.isSquareAttacked(move[2], move[3], targetColor, boardBinary, isWhiteOnBottom)) {
                if (pieceValue > bestCaptureValue) {
                    bestCaptureValue = pieceValue;
                    bestCapture = move;
                }
            }
        }
        return bestCapture;
    }

    private int evaluateBoard(int[] board) {
        int score = 0;
        int myMobility = 0;
        int oppMobility = 0;

        for (int i = 0; i < 8; i++) { // value of pieces and position
            for (int j = 0; j < 8; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, board);
                int pieceType = pieceBinary & 0b111;
                if (pieceType != 0) {
                    int pieceColor = (pieceBinary >> 3) & 0b11;
                    int pieceValue = BinaryBoard.getPieceValue(pieceBinary) * 110; // scale
                    int rowForPst = (pieceColor == 0) ? i : 7 - i; // white-perspective
                    int pstAdd = getPieceSquareTable(pieceType)[rowForPst][j];
                    int pieceScore = pieceValue + pstAdd;
                    if (pieceColor == color) {
                        score += pieceScore;
                    } else {
                        score -= pieceScore;
                    }

                    int opponentColor = (pieceColor == 0) ? 1 : 0;
                    boolean attacked = BinaryBoard.isSquareAttacked(i, j, opponentColor, board, isWhiteOnBottom);
                    boolean defended = BinaryBoard.isSquareAttacked(i, j, pieceColor, board, isWhiteOnBottom);
                    if (attacked) {
                        int hangingPenalty = BinaryBoard.getPieceValue(pieceBinary) * 150; // stronger penalty for hanging pieces
                        int defendedBonus = BinaryBoard.getPieceValue(pieceBinary) * 40; // bonus for defended pieces
                        if (defended) {
                            if (pieceColor == color) {
                                score += defendedBonus;
                            } else {
                                score -= defendedBonus;
                            }
                        } else {
                            if (pieceColor == color) {
                                score -= hangingPenalty;
                            } else {
                                score += hangingPenalty;
                            }
                        }
                    }
                }
            }
        }

        if (BinaryBoard.isKingInCheck(board, color, isWhiteOnBottom)) {
            score -= 800; // stronger penalty if our king is in check
        }
        if (BinaryBoard.isKingInCheck(board, (color == 0) ? 1 : 0, isWhiteOnBottom)) {
            if (isEndgamePosition(board)) {
                score += 250; // reward putting opponent king in check in endgame
            } else {
                score += 40; // smaller reward for check in opening/midgame since it's less likely to lead to
                             // immediate threats
            }
        }

        if (isEndgamePosition(board)) {
            score += evaluateEndgameKingAttack(board);
        }

        int centerControl = 0;
        for (int i = 2; i <= 5; i++) { // evaluate center control by rewarding pieces that control the central squares
            for (int j = 2; j <= 5; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, board);
                if (pieceBinary != 0) {
                    int pieceValue = BinaryBoard.getPieceValue(pieceBinary);
                    int pieceColor = (pieceBinary >> 3) & 0b11;
                    centerControl += (pieceColor == color) ? pieceValue * 20 : -pieceValue * 20; // Reward control of
                                                                                                 // the center
                }
            }
        }
        score += centerControl;

        myMobility = getAllPossibleMoves(board, color).size();
        int opponentColor = (color == 0) ? 1 : 0;
        oppMobility = getAllPossibleMoves(board, opponentColor).size();
        score += (myMobility - oppMobility) * 10;

        int pawnStructureScore = evaluatePawnStructure(board);
        score += pawnStructureScore;

        return score;
    }

    private boolean isEndgamePosition(int[] board) {
        int material = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, board);
                int pieceType = pieceBinary & 0b111;
                if (pieceType == 0 || pieceType == 1 || pieceType == 6) {
                    continue;
                }
                material += BinaryBoard.getPieceValue(pieceBinary);
            }
        }
        return material <= 10; // if total material (excluding pawns and kings) is 10 or less it is endgame
    }

    private int evaluateEndgameKingAttack(int[] board) {
        int opponentColor = (color == 0) ? 1 : 0;
        int[] kingSquare = BinaryBoard.getKingPosition(opponentColor, board);
        if (kingSquare == null) {
            return 0;
        }
        int bonus = 0;
        if (BinaryBoard.isSquareAttacked(kingSquare[0], kingSquare[1], color, board, isWhiteOnBottom)) {
            bonus += 70; // reward for attacking the opponent king in endgame
        }
        int kingRow = kingSquare[0];
        int kingCol = kingSquare[1];
        if (kingRow >= 2 && kingRow <= 5 && kingCol >= 2 && kingCol <= 5) {
            bonus += 15; // central king is easier to attack in endgame
        }
        return bonus;
    }

    private int evaluatePawnStructure(int[] board) { // evaluate pawn structure by counting doubled, isolated, and passed pawns
        int score = 0;
        int[] whitePawnsInBoard = new int[8];
        int[] blackPawnsInBoard = new int[8];
        ArrayList<int[]> whitePawnSquares = new ArrayList<>();
        ArrayList<int[]> blackPawnSquares = new ArrayList<>();

        for (int i = 0; i < 8; i++) { // count pawns in each column and store their positions for evaluating doubled, isolated, and passed pawns
            for (int j = 0; j < 8; j++) {
                int pieceBinary = BinaryBoard.getBinaryPiece(i, j, board);
                int pieceType = pieceBinary & 0b111;
                int pieceColor = (pieceBinary >> 3) & 0b11;
                if (pieceType == 1) { // pawn
                    if (pieceColor == 0) {
                        whitePawnsInBoard[j]++;
                        whitePawnSquares.add(new int[] { i, j });
                    } else {
                        blackPawnsInBoard[j]++;
                        blackPawnSquares.add(new int[] { i, j });
                    }
                }
            }
        }

        for (int j = 0; j < 8; j++) { // Doubled pawns penalty
            if (whitePawnsInBoard[j] > 1)
                score -= 20 * (whitePawnsInBoard[j] - 1);
            if (blackPawnsInBoard[j] > 1)
                score += 20 * (blackPawnsInBoard[j] - 1);
        }

        for (int a = 0; a < 8; a++) { // Isolated pawns penalty
            boolean whiteIsolated = whitePawnsInBoard[a] > 0
                    && ((a == 0 || whitePawnsInBoard[a - 1] == 0) && (a == 7 || whitePawnsInBoard[a + 1] == 0));
            boolean blackIsolated = blackPawnsInBoard[a] > 0
                    && ((a == 0 || blackPawnsInBoard[a - 1] == 0) && (a == 7 || blackPawnsInBoard[a + 1] == 0));
            if (whiteIsolated)
                score -= 15 * whitePawnsInBoard[a];
            if (blackIsolated)
                score += 15 * blackPawnsInBoard[a];
        }

        for (int[] square : whitePawnSquares) { // Passed pawns bonus
            int row = square[0];
            int col = square[1];
            int dir = isWhiteOnBottom ? -1 : 1; // forward for white
            boolean isPassed = true;
            int checkRow = row + dir;
            while (checkRow >= 0 && checkRow < 8) {
                for (int b = -1; b <= 1; b++) {
                    int c = col + b;
                    if (c < 0 || c > 7)
                        continue;
                    int pb = BinaryBoard.getBinaryPiece(checkRow, c, board);
                    if ((pb & 0b111) == 1 && ((pb >> 3) & 0b11) == 1) { // opposing pawn
                        isPassed = false;
                        break;
                    }
                }
                if (!isPassed)
                    break;
                checkRow += dir;
            }
            if (isPassed) { // more advanced pawns are worth more
                int advancement = isWhiteOnBottom ? (7 - row) : row;
                score += 40 + advancement * 5;
            }
        }
        for (int[] square : blackPawnSquares) {
            int row = square[0]; // row
            int col = square[1]; // column
            int dir = isWhiteOnBottom ? 1 : -1; // forward for black
            boolean isPassed = true;
            int checkRow = row + dir;
            while (checkRow >= 0 && checkRow < 8) {
                for (int b = -1; b <= 1; b++) { //
                    int c = col + b;
                    if (c < 0 || c > 7)
                        continue;
                    int pb = BinaryBoard.getBinaryPiece(checkRow, c, board);
                    if ((pb & 0b111) == 1 && ((pb >> 3) & 0b11) == 0) { // opposing pawn
                        isPassed = false;
                        break;
                    }
                }
                if (!isPassed)
                    break;
                checkRow += dir;
            }
            if (isPassed) {
                int advancement = isWhiteOnBottom ? (7 - row) : row;
                score -= 40 + advancement * 5;
            }
        }

        return score;
    }

    private int[][] getPieceSquareTable(int type) { // returns a table showing how good certain squares are for a piece
        switch (type) {
            case 1:
                return PAWN_PST;
            case 2:
                return ROOK_PST;
            case 3:
                return KNIGHT_PST;
            case 4:
                return BISHOP_PST;
            case 5:
                return QUEEN_PST;
            case 6:
                return KING_PST;
            default:
                return new int[8][8];
        }
    }

    private static final int[][] PAWN_PST = {
            { 0, 0, 0, 0, 0, 0, 0, 0 },
            { 50, 50, 50, 50, 50, 50, 50, 50 },
            { 10, 10, 20, 30, 30, 20, 10, 10 },
            { 5, 5, 10, 25, 25, 10, 5, 5 },
            { 0, 0, 0, 20, 20, 0, 0, 0 },
            { 5, -5, -10, 0, 0, -10, -5, 5 },
            { 5, 10, 10, -50, -50, 10, 10, 5 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };

    private static final int[][] KNIGHT_PST = {
            { -50, -40, -30, -30, -30, -30, -40, -50 },
            { -40, -20, 0, 5, 5, 0, -20, -40 },
            { -30, 5, 10, 15, 15, 10, 5, -30 },
            { -30, 0, 15, 20, 20, 15, 0, -30 },
            { -30, 5, 15, 20, 20, 15, 5, -30 },
            { -30, 0, 10, 15, 15, 10, 0, -30 },
            { -40, -20, 0, 0, 0, 0, -20, -40 },
            { -50, -40, -30, -30, -30, -30, -40, -50 }
    };

    private static final int[][] BISHOP_PST = {
            { -20, -10, -10, -10, -10, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 10, 10, 5, 0, -10 },
            { -10, 5, 5, 10, 10, 5, 5, -10 },
            { -10, 0, 10, 10, 10, 10, 0, -10 },
            { -10, 10, 10, 10, 10, 10, 10, -10 },
            { -10, 5, 0, 0, 0, 0, 5, -10 },
            { -20, -10, -10, -10, -10, -10, -10, -20 }
    };

    private static final int[][] ROOK_PST = {
            { 0, 0, 0, 5, 5, 0, 0, 0 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { -5, 0, 0, 0, 0, 0, 0, -5 },
            { 5, 10, 10, 10, 10, 10, 10, 5 },
            { 0, 0, 0, 0, 0, 0, 0, 0 }
    };

    private static final int[][] QUEEN_PST = {
            { -20, -10, -10, -5, -5, -10, -10, -20 },
            { -10, 0, 0, 0, 0, 0, 0, -10 },
            { -10, 0, 5, 5, 5, 5, 0, -10 },
            { -5, 0, 5, 5, 5, 5, 0, -5 },
            { 0, 0, 5, 5, 5, 5, 0, -5 },
            { -10, 5, 5, 5, 5, 5, 0, -10 },
            { -10, 0, 5, 0, 0, 0, 0, -10 },
            { -20, -10, -10, -5, -5, -10, -10, -20 }
    };

    private static final int[][] KING_PST = {
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -30, -40, -40, -50, -50, -40, -40, -30 },
            { -20, -30, -30, -40, -40, -30, -30, -20 },
            { -10, -20, -20, -20, -20, -20, -20, -10 },
            { 10, 0, -10, -10, -10, -10, 0, 10 },
            { 20, 30, 10, 0, 0, 10, 30, 20 }
    };
}
