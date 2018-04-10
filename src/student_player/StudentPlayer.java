package student_player;

import java.util.ArrayList;
import java.util.List;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** Player file submitted by Ivelin Bratanov (260535395) */
public class StudentPlayer extends TablutPlayer {
	
    /**
     * Constructor for StudentPlayer class
     */
    public StudentPlayer() {
        super("260535395");
    }

    /**
     * Selects a move using Minimax tree search and returns the best move found
     */
    public Move chooseMove(TablutBoardState boardState) {
        // Pass all legal moves for the current board state to Minimax algorithm.                
        return minimaxDecision(boardState.getAllLegalMoves(), boardState);
    }
    
    /**
     * This is the core method for selecting which move to play.
     * It follows the pseudo-code for minimaxDecision shown in the class slides.
     * 
     * It implements represents the initial branching of the current node, processing
     * each of the available moves and then calling minimaxValue() to evaluate the value
     * of the move by traversing the game state tree using minimax with alpha-beta pruning.
     */
    public TablutMove minimaxDecision(List<TablutMove> moves, TablutBoardState boardState) {
    	// Keeps track of when the move was started in order to avoid timeouts
    	long moveStartTime = System.currentTimeMillis();
    	
    	// Stores the value of every possible move for the current board state.
        double[] moveValue = new double [moves.size()];
    	
        // This was found to be the max depth value which causes minimal timeouts
    	int maxDepth = 3;
    	
    	/*
    	 * Iterate through all possible moves and assign a value to each of them using mimimax
    	 * (with α - β pruning) and the evaluation function.
    	 */
    	int curMoveIdx = 0;
    	for (TablutMove curMove: moves) {
    		
    		// Evaluating a move to depth 3 takes up to 60ms in most cases, thus if we reach 1940ms, return the best move found thus far
    		if (System.currentTimeMillis() - moveStartTime > 1940) {
    	    	return moves.get(getHighestValueMove(moveValue));
    		}

    		// Clone the board state and apply the move to obtain the new game state and evaluate it using minimax
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(curMove); // apply the operator o and obtain the new game state s.
            
            // Shortcut which exits if we find a winning move.
            if (clonedBoardState.getWinner() == player_id) {
            	return curMove;
            }
            
            // start with alpha & beta = -10K/10K and initial depth 1
            moveValue[curMoveIdx] = minimaxValue(clonedBoardState, -10000, 10000, 1, maxDepth); // Value[o] = MinimaxValue(s)
            curMoveIdx++;
    	}
    	
    	//return the operator with the highest value Value[o] by finding its index in the moves list
    	return moves.get(getHighestValueMove(moveValue));
    }
    
    /**
     * Helper method to iterate through the moveValue array and return
     * the index of the highest valued move. 
     */
    public int getHighestValueMove(double [] moveValue) {
    	double maxValue = moveValue[0];
    	int maxIdx = 0; 
    	for(int i = 1; i < moveValue.length; i++) {
    		if (moveValue[i] > maxValue) {
    			maxValue = moveValue[i];
    			maxIdx = i;
    		}
    	}
    	return maxIdx;
    }
    
    /**
     * This implementation of the MiniMaxValue() method shown in class
     * is a hybrid between the pseudocode of minimax and the code for alpha beta pruning,
     * as this implementation contains alpha beta pruning.
     * 
     * It traverses the game state tree by generating successor states recursively and evaluating
     * "leaf" nodes when the maximum intended depth is reached.
     */
    public double minimaxValue(TablutBoardState boardState, double alpha, double beta, int depth, int maxDepth) {
    	// if isTerminal(s), return Utility(s) based on board state
    	if (boardState.gameOver()) {
    		int winner = boardState.getWinner();
    		if (winner == player_id) {
    			return 50000;
    		} else if (winner == 1 - player_id) {
    			return -50000;
    		} else {
    			// In the case of a draw we return 0 because we'll have reached 100 moves and all other leaves will either be win, loss, or draw
        		return 0;
    		}
    	} else if (depth == maxDepth) { // If we've reached the maximum decided depth, evaluate and return this value
    		return MyTools.evaluation(boardState, player_id);
    	} else { // Otherwise, continue to generate and explore the search tree
    		ArrayList<TablutBoardState> successors = getSuccessors(boardState);
        	
    		if (player_id == boardState.getTurnPlayer()) { // if Max player is to move in s, return maxs’ Value(s’).
            	for (TablutBoardState sucState: successors) { // for each state s’ in Successors(s)
        			// let α = max { α, MinValues(s’,α,β) }.
            		alpha = Math.max(alpha, minimaxValue(sucState, alpha, beta, depth + 1, maxDepth)); // let Value(s’) = MinimaxValue(s’)
            		if (alpha >= beta) { // if α ≥ β, return β.
            			return beta;
            		}
            	}

    			return alpha; // return α.
    		} else { // if Min player is to move in s, return mins’ Value(s’).

            	for (TablutBoardState sucState: successors) { // for each state s’ in Successors(s)
            		// let β = min { β, MinValues(s’,α,β) }.
            		beta = Math.min(beta, minimaxValue(sucState, alpha, beta, depth + 1, maxDepth)); // let Value(s’) = MinimaxValue(s’)
            		if (alpha >= beta) { // if α ≥ β, return α.
            			return alpha;
            		}
            	}
            	
    			return beta; //return β.
    		}        	
    	}
    }
    
    /**
     * Retrieves all successor states for a given board state by applying
     * all legal moves to clones of the current board state.
     */
    public ArrayList<TablutBoardState> getSuccessors(TablutBoardState boardState) {
    	ArrayList<TablutBoardState> successors = new ArrayList<TablutBoardState>();
    	
    	// Iterate through all legal moves and apply them to a clone of the board state
    	for (TablutMove curMove: boardState.getAllLegalMoves()) {
    		TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
    		clonedBoardState.processMove(curMove); // apply the operator o and obtain the new game state s.
    		successors.add(clonedBoardState);
    	}

    	return successors;
    }
    
    /**
     * The basic evaluation function used initially (piece difference).
     * Kept for testing purposes.
     */
    public double basicEvaluation(TablutBoardState boardState) {    	
        // Calculate the difference between the player's pieces and the opponent's pieces
    	double pieceDifference = (double) (boardState.getNumberPlayerPieces(player_id) - boardState.getNumberPlayerPieces(player_id - 1));

		return pieceDifference;
    }
}