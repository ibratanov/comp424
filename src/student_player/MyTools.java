package student_player;

import java.util.ArrayList;
import java.util.List;

import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutBoardState.Piece;
import tablut.TablutMove;

/**
 * This class is mostly used to keep methods that are used in the
 * the game state evaluation function used by Minimax
 */
public class MyTools {
	
	/**
     * The evaluation function for the board state. Although several
     * different functions were considered and tested, along with various weights
     * for the different variables, the evaluation function below turned out to
     * work best. An analysis of evaluation functions is included in the report.
     */
	public static double evaluation(TablutBoardState boardState, int player_id) {    	
        int opponent = 1 - player_id;
        double oppPieceValue = (double) boardState.getNumberPlayerPieces(opponent);
        double yourPieceValue = (double) boardState.getNumberPlayerPieces(player_id);
        

        int turn = boardState.getTurnNumber();
		/*
		 * The evaluation function is different when the player is a Swede vs when the player is a Muscovite.
		 * 
		 * The following differences in player priority were determined to be the most effective:
		 * Swedes care less about the number of enemy pieces surrounding the king and more about the King's
		 * proximity to a corner (in number of moves).
		 * 
		 * The Muscovites care more about the piece count difference as the game progresses and less about
		 * the king's proximity to a corner.
		 */
		double evaluationValue = 0.0;
    	if (player_id == TablutBoardState.SWEDE) {
			/*
			 * The Swedes evaluation function becomes more aggressive as the game progresses in terms of:
			 * the number of moves for the king to reach a corner
			 */
			if (turn < 40) {
				evaluationValue = yourPieceValue - oppPieceValue + kingMovesToCornerValue(boardState);
			} else if (turn < 70) {
				evaluationValue = yourPieceValue - oppPieceValue + 2.0 * kingMovesToCornerValue(boardState);
    		} else {
    			evaluationValue = yourPieceValue - oppPieceValue + 3.0 * kingMovesToCornerValue(boardState);
    		}
		} else {
			/*
			 * The Muscovites evaluation function becomes more aggressive as the game progresses in terms of:
			 * the number of enemy pieces around the king and
			 * the number of moves for the king to reach a corner
			 */
			if (turn < 40) {
				evaluationValue = yourPieceValue - oppPieceValue + piecesAroundCorners(boardState) - kingMovesToCornerValue(boardState) + enemyPiecesAroundKing(boardState);
			} else if (turn < 70) {
				evaluationValue = 2 * (yourPieceValue - oppPieceValue) + piecesAroundCorners(boardState) - 1.5 * kingMovesToCornerValue(boardState) + 6 * enemyPiecesAroundKing(boardState);
    		} else {
				evaluationValue = 3 * (yourPieceValue - oppPieceValue) + piecesAroundCorners(boardState) - 1.5 * kingMovesToCornerValue(boardState) + 12 * enemyPiecesAroundKing(boardState);
    		}
		}

    	return evaluationValue;
    }
	
	/**
	 * Given a game state, this method returns a value used in board state evaluation
	 * which represents the number of enemy pieces surrounding the Swede king.
	 */
	public static double enemyPiecesAroundKing(TablutBoardState boardState) {
		double numPieces = 0.0;
		
		List<Coord> kingNeighbors = Coordinates.getNeighbors(boardState.getKingPosition());
		
		for (Coord c : kingNeighbors) {
			if (boardState.getPieceAt(c) == Piece.BLACK) {
				numPieces += 0.25;
			}
		}
		
		return numPieces;
	}
	
	/**
	 * Given a game state, this method returns a value used in board state evaluation for the
	 * number of black pieces in key strategic locations near the corners.
	 * 
	 * These locations were determined to be useful for the Muscovites in
	 * maintaining an advantage during the game.
	 */
	public static double piecesAroundCorners(TablutBoardState boardState) {
		double value = 0.0;
		
		// Top left corner
		if (boardState.getPieceAt(1, 1) == Piece.BLACK) {
			value += 0.25;
		}
		
		// Top right corner
		if (boardState.getPieceAt(1, 7) == Piece.BLACK) {
			value += 0.25;
		}
		
		// Bottom left corner
		if (boardState.getPieceAt(7, 1) == Piece.BLACK) {
			value += 0.25;
		}
		
		// Bottom right corner
		if (boardState.getPieceAt(7, 7) == Piece.BLACK) {
			value += 0.25;
		}
		
		return value;
	}
	
	/**
	 * This method returns a value representing the number of king moves to all the corners.
	 * Given a state, the method checks the min number of moves to each corner, and returns
	 * a positive value if we are within 1-2 moves to a certain corner, and an even higher
	 * value if we are withing 1-2 moves to more than one corner.
	 * 
	 */
    public static double kingMovesToCornerValue(TablutBoardState boardState) {
    	Coord kingPosition = boardState.getKingPosition();
    	
    	// Retrieves all legal moves for the king based on its current position
    	List<TablutMove> kingMoves = getLegalKingMovesForPosition(kingPosition, boardState);
    	
    	double moveDistanceValue = 0.0;
    	if (!kingMoves.isEmpty()) { // If the king actually has moves        	
        	
    		// Stores the min number of moves to reach each of the 4 corners
    		int [] distances = new int [4];
        	
    		// Iterate through all corners, calculating the min number of moves to reach each one
    		int cornerIdx = 0;
        	for (Coord corner : Coordinates.getCorners()) {
        		distances[cornerIdx] = calcMinMovesToCorner(boardState, corner, 1, kingPosition);
        		cornerIdx++;
        	}

        	// Generate the move's value based on proximity to the corner
        	for (int i = 0; i < distances.length; i++) {
        		switch (distances[i]) {
                case 1:  moveDistanceValue += 15; // Being 1 move away is much more valuable
                         break;
                case 2:  moveDistanceValue += 1;
                         break;
                default: moveDistanceValue += 0;
                         break;
        		}
        	}
    	}
    	    	
    	return moveDistanceValue;
    }
    
    /**
     * This method calculates the min number of moves for the king to reach a given corner.
     * 
     * This is done by ignoring opponent moves. We simply care about how many consecutive moves it would
     * take the king to reach a specific corner. This is because it becomes very difficult (and costly) to
     * predict opponent moves as well.
     * 
     * This method projects a move onto the board state and recursively goes to the following move, but does
     * not actually process the move in order to be more efficient (time and memory-wise).
     */
    public static int calcMinMovesToCorner(TablutBoardState boardState, Coord corner, int moveCt, Coord kingPosition) {    	
    	// Termination condition - either we're in a corner or it takes too many moves and thus becomes irrelevant
    	if (moveCt == 3 || Coordinates.isCorner(kingPosition)) {
    		return moveCt;
    	}
    	
    	List<TablutMove> kingMoves = getLegalKingMovesForPosition(kingPosition, boardState);
    	
    	// We'll store the counts for each move here
    	int [] moveCounts = new int[kingMoves.size()];

    	// Iterate through current possible king moves and see how much closer we can get to a corner
    	int moveIdx = 0;
    	for (TablutMove move : kingMoves) {
			// If move brings you closer to the corner, attempt it
			if (move.getStartPosition().distance(corner) > move.getEndPosition().distance(corner)) {            	
                moveCounts[moveIdx] = calcMinMovesToCorner(boardState, corner, moveCt + 1, move.getEndPosition());
                moveIdx++;
			}
    	}
    	
    	// Find the min number of moves to reach the corner, or return 50 if unreachable
    	int min = 50;
    	for (int i = 0; i < moveCounts.length; i++) {
    		int current = moveCounts[i];
    		if (current != 0 && current < min) {
    			min = current;
    		}
    	}
    	return min;
    }
    
    /**
     * Modified the getLegalMovesForPosition() method from TablutBoardState.java,
     * adjusted because the original was not permitting me to retrieve king moves.
     */
    public static ArrayList<TablutMove> getLegalKingMovesForPosition(Coord start, TablutBoardState boardState) {
        ArrayList<TablutMove> legalMoves = new ArrayList<>();

        // Iterate along 4 directions.
        List<Coord> goodCoords = new ArrayList<>();
        goodCoords.addAll(getLegalCoordsInDirection(start, -1, 0, boardState)); // move in -x direction
        goodCoords.addAll(getLegalCoordsInDirection(start, 0, -1, boardState)); // move in -y direction
        goodCoords.addAll(getLegalCoordsInDirection(start, 1, 0, boardState)); // move in +x direction
        goodCoords.addAll(getLegalCoordsInDirection(start, 0, 1, boardState)); // move in +y direction

        /*
         * Add the real moves now. We do not call isLegal here; this is because we
         * efficiently enforce legality by only adding those that are legal. This makes
         * for a more efficient method so people aren't slowed down by just figuring out
         * what they can do.
         */
        for (Coord end : goodCoords) {
            legalMoves.add(new TablutMove(start, end, 1));
        }
        return legalMoves;
    }
    
    /**
     * Added the getLegalCoordsInDirection() method from TablutBoardState.java,
     * because it is required by getLegalKingMovesForPosition() (above) but the original is private.
     */
    private static List<Coord> getLegalCoordsInDirection(Coord start, int x, int y, TablutBoardState boardState) {
        ArrayList<Coord> coords = new ArrayList<>();
        assert (!(x != 0 && y != 0));
        int startPos = (x != 0) ? start.x : start.y; // starting at x or y
        int incr = (x != 0) ? x : y; // incrementing the x or y value
        int endIdx = (incr == 1) ? 9 - 1 : 0; // moving in the 0 or 8 direction

        for (int i = startPos + incr; incr * i <= endIdx; i += incr) { // increasing/decreasing functionality
            // new coord is an x coord change or a y coord change
            Coord coord = (x != 0) ? Coordinates.get(i, start.y) : Coordinates.get(start.x, i);
            if (boardState.coordIsEmpty(coord)) {
                coords.add(coord);
            } else {
                break;
                
            }
        }
        return coords;
    }
}