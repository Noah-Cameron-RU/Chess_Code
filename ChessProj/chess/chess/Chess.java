// Noah Cameron nwc21
// Cierra Wickliff clw210

package chess;

import java.util.ArrayList;
// Partners:
// Noah Cameron
// Cierra Wickliff
// Current issue: We need the game to continue AFTER illegal moves
// Now it continues, but pieces can capture their own pieces

public class Chess {
    private static int lastMoveStartRow = -1;
    private static int lastMoveStartCol = -1;
    private static int lastMoveEndRow = -1;
    private static int lastMoveEndCol = -1;
    private static boolean whiteKingMoved = false;
    private static boolean blackKingMoved = false;
    private static boolean whiteRookLeftMoved = false;
    private static boolean whiteRookRightMoved = false;
    private static boolean blackRookLeftMoved = false;
    private static boolean blackRookRightMoved = false;


    enum Player { white, black }

    private static ReturnPiece[][] board = new ReturnPiece[8][8];
    private static Player currentPlayer = Player.white;

    /**
     * Plays the next move for whichever player has the turn.
     * 
     * @param move String for next move, e.g. "a2 a3"
     * @return A ReturnPlay instance that contains the result of the move.
     */
    public static ReturnPlay play(String move) {
        if(move.equals("resign")) {
            if(currentPlayer == Player.white) {
                ReturnPlay rp = getBoardState(); //This keep the board unchanged here
                rp.message = ReturnPlay.Message.RESIGN_BLACK_WINS;
                return rp;
            } else {
                ReturnPlay rp = getBoardState(); //This keep the board unchanged here
                rp.message = ReturnPlay.Message.RESIGN_WHITE_WINS;
                return rp;
            }
        }
    
        String[] moveParts = move.split(" ");
        if (moveParts.length < 2 || moveParts.length > 3) {
            // System.out.println("Invalid move format.");
            return retryMove();
        }

        // Validate promotion piece
        String promotionPiece = (moveParts.length == 3 && (!moveParts[2].equals("draw?"))) ? moveParts[2] : "Q";
        String validPromotions = "QRBN";
        if (!validPromotions.contains(promotionPiece.toUpperCase())) {
            promotionPiece = "Q";  // Default to Queen if invalid
        }
    
        String origin = moveParts[0];
        String destination = moveParts[1];
    
        int startFile = origin.charAt(0) - 'a';
        int startRank = 8 - Character.getNumericValue(origin.charAt(1));
        int endFile = destination.charAt(0) - 'a';
        int endRank = 8 - Character.getNumericValue(destination.charAt(1));
    
        // System.out.println("Attempting move: " + origin + " -> " + destination);
    
        if (board[startRank][startFile] == null) {
            return retryMove();
        }
    
        boolean isWhitePiece = board[startRank][startFile].pieceType.toString().startsWith("W");
        if ((currentPlayer == Player.white && !isWhitePiece) || (currentPlayer == Player.black && isWhitePiece)) {
            return retryMove();
        }
    
        if (!isValidMove(startRank, startFile, endRank, endFile)) {
            return retryMove();
        }
    
        // Simulate move to check if it leaves the king in check
        if (doesMoveLeaveKingInCheck(startRank, startFile, endRank, endFile)) {
            return retryMove();
        }

    
        // Execute move
        // ReturnPiece capturedPiece = board[endRank][endFile];

        if (board[startRank][startFile].pieceType == ReturnPiece.PieceType.WK || board[startRank][startFile].pieceType == ReturnPiece.PieceType.BK) {
            // System.out.println("MADE IT HEREEEEE");
            if(isValidCastling(startRank, startFile, endRank, endFile)) {
                // System.out.println("Castling move detected!");

                if (currentPlayer == Player.white) {
                    if (endFile == 6) { // Kingside Castling (White: e1 → g1, Rook: h1 → f1)
                        board[7][4] = null;
                        board[7][6] = createPiece(ReturnPiece.PieceType.WK, 6, 1);
                        board[7][7] = null;
                        board[7][5] = createPiece(ReturnPiece.PieceType.WR, 5, 1); // Move Rook
                        whiteKingMoved = true;
                        whiteRookRightMoved = true;
                    } else if (endFile == 2) { // Queenside Castling (White: e1 → c1, Rook: a1 → d1)
                        board[7][4] = null;
                        board[7][2] = createPiece(ReturnPiece.PieceType.WK, 2, 1);
                        board[7][0] = null;
                        board[7][3] = createPiece(ReturnPiece.PieceType.WR, 3, 1); // Move Rook
                        whiteKingMoved = true;
                        whiteRookLeftMoved = true;
                    }
                } else { // Black's Castling
                    if (endFile == 6) { // Kingside Castling (Black: e8 → g8, Rook: h8 → f8)
                        board[0][4] = null;
                        board[0][6] = createPiece(ReturnPiece.PieceType.BK, 6, 8);
                        board[0][7] = null;
                        board[0][5] = createPiece(ReturnPiece.PieceType.BR, 5, 8); // Move Rook
                        blackKingMoved = true;
                        blackRookRightMoved = true;
                    } else if (endFile == 2) { // Queenside Castling (Black: e8 → c8, Rook: a8 → d8)
                        board[0][4] = null;
                        board[0][2] = createPiece(ReturnPiece.PieceType.BK, 2, 8);
                        board[0][0] = null;
                        board[0][3] = createPiece(ReturnPiece.PieceType.BR, 3, 8); // Move Rook
                        blackKingMoved = true;
                        blackRookLeftMoved = true;
                    }
                }
            }
        } else {
            board[endRank][endFile] = board[startRank][startFile];
            board[startRank][startFile] = null;
        }

        // Handle pawn promotion **before updating the piece file and rank**
        if (board[endRank][endFile] != null) {
            if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.WP && endRank == 0) {
                board[endRank][endFile] = promotePawn(promotionPiece, endFile, endRank, true);
            } 
            else if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.BP && endRank == 7) {
                board[endRank][endFile] = promotePawn(promotionPiece, endFile, endRank, false);
            }
        }
    
        board[endRank][endFile].pieceRank = 8 - endRank;
        board[endRank][endFile].pieceFile = ReturnPiece.PieceFile.values()[endFile];
    
        // System.out.println("Move executed successfully!");
    
        lastMoveStartRow = startRank;
        lastMoveStartCol = startFile;
        lastMoveEndRow = endRank;
        lastMoveEndCol = endFile;

        // Track if king or rooks moved
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.WK) {
            whiteKingMoved = true;
        }
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.BK) {
            blackKingMoved = true;
        }
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.WR && startFile == 0) {
            whiteRookLeftMoved = true;
        }
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.WR && startFile == 7) {
            whiteRookRightMoved = true;
        }
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.BR && startFile == 0) {
            blackRookLeftMoved = true;
        }
        if (board[endRank][endFile].pieceType == ReturnPiece.PieceType.BR && startFile == 7) {
            blackRookRightMoved = true;
        }

        // This is for the castling portion 
        

        currentPlayer = (currentPlayer == Player.white) ? Player.black : Player.white;
    
        if (isKingInCheck(currentPlayer)) {
            if (isCheckmate(currentPlayer)) {
                if(currentPlayer == Player.white) {
                    ReturnPlay rp = getBoardState(); //This keep the board unchanged here
                    rp.message = ReturnPlay.Message.CHECKMATE_WHITE_WINS;
                    return rp;
                }
                if(currentPlayer == Player.black) {
                    ReturnPlay rp = getBoardState(); //This keep the board unchanged here
                    rp.message = ReturnPlay.Message.CHECKMATE_BLACK_WINS;
                    return rp;
                }
            } else {
                ReturnPlay rp = getBoardState(); //This keep the board unchanged here
                rp.message = ReturnPlay.Message.CHECK;
                return rp;
            }
        }

        if(moveParts.length == 3 && moveParts[2].equals("draw?")) {
            ReturnPlay rp = getBoardState(); //This keep the board unchanged here
            rp.message = ReturnPlay.Message.DRAW;
            return rp;
        }

    
        return getBoardState();
    }
    
    private static boolean doesMoveLeaveKingInCheck(int startRow, int startCol, int endRow, int endCol) {
        ReturnPiece temp = board[endRow][endCol];
        board[endRow][endCol] = board[startRow][startCol];
        board[startRow][startCol] = null;
    
        boolean stillInCheck = isKingInCheck(currentPlayer);
    
        board[startRow][startCol] = board[endRow][endCol];
        board[endRow][endCol] = temp;
    
        return stillInCheck;
    }
    
    private static boolean isKingInCheck(Player player) {
        int kingRow = -1, kingCol = -1;
        ReturnPiece.PieceType kingType = (player == Player.white) ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;
    
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].pieceType == kingType) {
                    kingRow = r;
                    kingCol = c;
                    break;
                }
            }
        }
    
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].pieceType.toString().charAt(0) != kingType.toString().charAt(0)) {
                    if (isValidMove(r, c, kingRow, kingCol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean isCheckmate(Player player) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null && board[r][c].pieceType.toString().charAt(0) == (player == Player.white ? 'W' : 'B')) {
                    for (int newR = 0; newR < 8; newR++) {
                        for (int newC = 0; newC < 8; newC++) {
                            if (isValidMove(r, c, newR, newC)) {
                                ReturnPiece temp = board[newR][newC];
                                board[newR][newC] = board[r][c];
                                board[r][c] = null;
                                boolean stillInCheck = isKingInCheck(player);
                                board[r][c] = board[newR][newC];
                                board[newR][newC] = temp;
                                if (!stillInCheck) return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    

    private static ReturnPlay retryMove() {
        ReturnPlay rp = getBoardState(); //This keep the board unchanged here
        rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return rp;
    }
    

    /**
     * This method resets the game and starts from scratch.
     */
    public static void start() {
        board = new ReturnPiece[8][8];
        currentPlayer = Player.white;
        initializeBoard();
    }

    /**
     * Initializes the board with pieces in their starting positions.
     */
    private static void initializeBoard() {
        // Pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = createPiece(ReturnPiece.PieceType.BP, i, 7);
            board[6][i] = createPiece(ReturnPiece.PieceType.WP, i, 2);
        }

        // Rooks
        board[0][0] = createPiece(ReturnPiece.PieceType.BR, 0, 8);
        board[0][7] = createPiece(ReturnPiece.PieceType.BR, 7, 8);
        board[7][0] = createPiece(ReturnPiece.PieceType.WR, 0, 1);
        board[7][7] = createPiece(ReturnPiece.PieceType.WR, 7, 1);

        // Knights
        board[0][1] = createPiece(ReturnPiece.PieceType.BN, 1, 8);
        board[0][6] = createPiece(ReturnPiece.PieceType.BN, 6, 8);
        board[7][1] = createPiece(ReturnPiece.PieceType.WN, 1, 1);
        board[7][6] = createPiece(ReturnPiece.PieceType.WN, 6, 1);

        // Bishops
        board[0][2] = createPiece(ReturnPiece.PieceType.BB, 2, 8);
        board[0][5] = createPiece(ReturnPiece.PieceType.BB, 5, 8);
        board[7][2] = createPiece(ReturnPiece.PieceType.WB, 2, 1);
        board[7][5] = createPiece(ReturnPiece.PieceType.WB, 5, 1);

        // Queens
        board[0][3] = createPiece(ReturnPiece.PieceType.BQ, 3, 8);
        board[7][3] = createPiece(ReturnPiece.PieceType.WQ, 3, 1);

        // Kings
        board[0][4] = createPiece(ReturnPiece.PieceType.BK, 4, 8);
        board[7][4] = createPiece(ReturnPiece.PieceType.WK, 4, 1);
    }

    private static ReturnPiece createPiece(ReturnPiece.PieceType type, int file, int rank) {
        ReturnPiece piece = new ReturnPiece();
        piece.pieceType = type;
        piece.pieceFile = ReturnPiece.PieceFile.values()[file];
        piece.pieceRank = rank;
        return piece;
    }

    private static boolean isValidMove(int startRow, int startCol, int endRow, int endCol) {
        ReturnPiece piece = board[startRow][startCol];
        if (piece == null) return false;
    
        String type = piece.pieceType.toString();
        // Testing pront lines:
         // System.out.println("Testing move for: " + piece.pieceType);

        ReturnPiece targetPiece = board[endRow][endCol];
        if (targetPiece != null) {
            boolean isSameColor = piece.pieceType.toString().charAt(0) == targetPiece.pieceType.toString().charAt(0);
    
            if (isSameColor) {
                // System.out.println("Illegal move: Cannot capture own piece.");
                return false;
            } else {
                 //System.out.println("Capturing opponent's piece: " + targetPiece.pieceType);
            }
        }
        
        if (type.equals("WK") || type.equals("BK")) {
            return isValidKingMove(startRow, startCol, endRow, endCol) || isValidCastling(startRow, startCol, endRow, endCol);
        }
        if (type.equals("WP")) {
            return isValidWhitePawnMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("BP")) {
            return isValidBlackPawnMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("WR") || type.equals("BR")) {
            return isValidRookMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("WN") || type.equals("BN")) {
            return isValidKnightMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("WB") || type.equals("BB")) {
            return isValidBishopMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("WQ") || type.equals("BQ")) {
            return isValidQueenMove(startRow, startCol, endRow, endCol);
        } else if (type.equals("WK") || type.equals("BK")) {
            return isValidKingMove(startRow, startCol, endRow, endCol);
        }


        
        return false;
        
    }

    private static boolean isValidCastling(int startRow, int startCol, int endRow, int endCol) {
        if (startRow != endRow) return false; // Should must stay in the same row
    
        boolean isWhite = (board[startRow][startCol].pieceType == ReturnPiece.PieceType.WK);
        boolean isBlack = (board[startRow][startCol].pieceType == ReturnPiece.PieceType.BK);
    
        if (isWhite) {
            if (whiteKingMoved) return false; // Cant casle if king has moved
    
            // Kingside castling (e1 to g1)
            if (startCol == 4 && endCol == 6 && !whiteRookRightMoved) {
                if (board[7][5] == null && board[7][6] == null) { // Checks if pieces between
                    return true;
                }
            }
            // Queenside castling like e1 to c1
            if (startCol == 4 && endCol == 2 && !whiteRookLeftMoved) {
                if (board[7][1] == null && board[7][2] == null && board[7][3] == null) { // No pieces between
                    return true;
                }
            }
        } 
        else if (isBlack) {
            if (blackKingMoved) return false; // King has moved before
    
            // Kingside castling (e8 to g8)
            if (startCol == 4 && endCol == 6 && !blackRookRightMoved) {
                if (board[0][5] == null && board[0][6] == null) { // No pieces between
                    return true;
                }
            }
            // Queenside castling (e8 to c8)
            if (startCol == 4 && endCol == 2 && !blackRookLeftMoved) {
                if (board[0][1] == null && board[0][2] == null && board[0][3] == null) { // No pieces between
                    return true;
                }
            }
        }
    
        return false;
    }

    private static boolean isValidWhitePawnMove(int startRow, int startCol, int endRow, int endCol) {
        // System.out.println("White Pawn Move Attempt: [" + startRow + "][" + startCol + "] → [" + endRow + "][" + endCol + "]");
    
        // Standard Forward Move (only if the square is empty)
        if (startCol == endCol) { 
            if (startRow - endRow == 1 && board[endRow][endCol] == null) {
                return true;
            }
            if (startRow == 6 && startRow - endRow == 2 && board[endRow][endCol] == null && board[5][endCol] == null) {
                return true;
            }
        }
    
        // Standard Diagonal Capture
        if (Math.abs(startCol - endCol) == 1 && startRow - endRow == 1) {
            if (board[endRow][endCol] != null) { // Ensure there is a piece to capture
                String capturedPiece = board[endRow][endCol].pieceType.toString();
                if (capturedPiece.startsWith("B")) { // Ensure it's an opponent's piece
                    // System.out.println("Valid Capture: Captured " + capturedPiece);
                    return true;
                }
            }
        }
    
        // En Passant why doesnt this workkkkkkkkk ahhh
        if (Math.abs(startCol - endCol) == 1 && startRow - endRow == 1 && board[endRow][endCol] == null) {
            int opponentRow = startRow;
            int opponentCol = endCol;
    
            if (board[opponentRow][opponentCol] != null && board[opponentRow][opponentCol].pieceType == ReturnPiece.PieceType.BP) {
                if (lastMoveStartRow == 1 && lastMoveEndRow == 3 && lastMoveStartCol == opponentCol) {
                    // System.out.println("En Passant: Capturing " + board[opponentRow][opponentCol].pieceType);
                    board[opponentRow][opponentCol] = null; // Remove captured pawn
                    return true;
                }
            }
        }
    
        return false;
    }
    
    
    
    
    private static boolean isValidBlackPawnMove(int startRow, int startCol, int endRow, int endCol) {
        // System.out.println("Black Pawn Move Attempt: [" + startRow + "][" + startCol + "] → [" + endRow + "][" + endCol + "]");
    
        // Standard Forward Move (only if the square is empty)
        if (startCol == endCol) { 
            if (endRow - startRow == 1 && board[endRow][endCol] == null) {
                return true; // Normal one-step move
            }
            if (startRow == 1 && endRow - startRow == 2 && board[endRow][endCol] == null && board[2][endCol] == null) {
                return true; // Two-step move from starting position
            }
        }
    
        // Standard Diagonal Capture
        if (Math.abs(startCol - endCol) == 1 && endRow - startRow == 1) {
            if (board[endRow][endCol] != null) { // Ensure there is a piece to capture
                String capturedPiece = board[endRow][endCol].pieceType.toString();
                if (capturedPiece.startsWith("W")) { // Ensure it's an opponent's piece
                    // System.out.println("Valid Capture: Captured " + capturedPiece);
                    return true;
                }
            }
        }
    
        // En Passant (remains unchanged)
        if (Math.abs(startCol - endCol) == 1 && endRow - startRow == 1 && board[endRow][endCol] == null) {
            int opponentRow = startRow;
            int opponentCol = endCol;
    
            if (board[opponentRow][opponentCol] != null && board[opponentRow][opponentCol].pieceType == ReturnPiece.PieceType.WP) {
                if (lastMoveStartRow == 6 && lastMoveEndRow == 4 && lastMoveStartCol == opponentCol) {
                    // System.out.println("En Passant: Capturing " + board[opponentRow][opponentCol].pieceType);
                    board[opponentRow][opponentCol] = null; // Remove captured pawn
                    return true;
                }
            }
        }
    
        return false;
    }
    
    
    

    private static boolean isValidRookMove(int startRow, int startCol, int endRow, int endCol) {
        if (startRow != endRow && startCol != endCol) return false; // Rooks move straight
        return isPathClear(startRow, startCol, endRow, endCol);
    }
    
    private static boolean isValidBishopMove(int startRow, int startCol, int endRow, int endCol) {
        if (Math.abs(startRow - endRow) != Math.abs(startCol - endCol)) return false; // Must be diagonal
        return isPathClear(startRow, startCol, endRow, endCol);
    }

    private static boolean isValidQueenMove(int startRow, int startCol, int endRow, int endCol) {
        // System.out.println("Queen Move Attempt: [" + startRow + "][" + startCol + "] → [" + endRow + "][" + endCol + "]");
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);
    
        boolean isStraightMove = (startRow == endRow || startCol == endCol);
        boolean isDiagonalMove = (rowDiff == colDiff);
    
        if (!isStraightMove && !isDiagonalMove) {
            // System.out.println("Invalid: Queen must move straight or diagonally.");
            return false;
        }
        boolean pathClear = isPathClear(startRow, startCol, endRow, endCol);
        // System.out.println("Path Clear: " + pathClear);
        
        return pathClear;
    }
    

    private static boolean isValidKnightMove(int startRow, int startCol, int endRow, int endCol) {
        int rowDiff = Math.abs(startRow - endRow);
        int colDiff = Math.abs(startCol - endCol);
        // System.out.println("Validating move ffor knight");
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    private static boolean isValidKingMove(int startRow, int startCol, int endRow, int endCol) {
        return Math.abs(startRow - endRow) <= 1 && Math.abs(startCol - endCol) <= 1;
    }
    
    
    private static boolean isPathClear(int startRow, int startCol, int endRow, int endCol) {
        int rowStep = Integer.compare(endRow, startRow); // -1, 0, or 1
        int colStep = Integer.compare(endCol, startCol); // -1, 0, or 1
    
        int row = startRow + rowStep;
        int col = startCol + colStep;
        while (row != endRow || col != endCol) {
            if (board[row][col] != null) return false;
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    private static ReturnPiece promotePawn(String promotionPiece, int file, int rank, boolean isWhite) {
        ReturnPiece.PieceType newPieceType;
    
        switch (promotionPiece.toUpperCase()) {
            case "R":
                newPieceType = isWhite ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
                break;
            case "B":
                newPieceType = isWhite ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
                break;
            case "N":
                newPieceType = isWhite ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
                break;
            case "Q":
            default:
                newPieceType = isWhite ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
                break;
        }
    
        // System.out.println("Pawn Promoted to " + newPieceType);
    
        ReturnPiece newPiece = new ReturnPiece();
        newPiece.pieceType = newPieceType;
        newPiece.pieceFile = ReturnPiece.PieceFile.values()[file];
        newPiece.pieceRank = rank + 1; // Convert board index to rank notation
        return newPiece;
    }
    
    

    private static ReturnPlay invalidMove() {
        ReturnPlay rp = new ReturnPlay();
        rp.message = ReturnPlay.Message.ILLEGAL_MOVE;
        return rp;
    }

    private static ReturnPlay getBoardState() {
        ReturnPlay rp = new ReturnPlay();
        rp.piecesOnBoard = new ArrayList<>();
    
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (board[r][c] != null) {
                    // System.out.println("Piece at [" + r + "][" + c + "]: " + board[r][c].pieceType);
                    rp.piecesOnBoard.add(board[r][c]);
                }
            }
        }
        return rp;
    }
    
}
