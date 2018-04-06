package student_player;

import java.util.ArrayList;
import java.util.List;

import boardgame.Move;
import coordinates.Coord;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends TablutPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260535395");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(TablutBoardState boardState) {
        // You probably will make separate functions in MyTools.
        // For example, maybe you'll need to load some pre-processed best opening
        // strategies...
//        MyTools.getSomething();

        // Get the legal moves for the current board state.        
        List<TablutMove> moves = boardState.getAllLegalMoves();
        
        return minimaxDecision(moves, boardState);
    }
    
    /**
     * This is the core method for selecting which move to play.
     * It follows the pseudo-code for minimaxDecision shown in the class slides.
     * 
     * It implements a sort of iterative deepening and represents the initial branching of the current node,
     * then calling minimaxValue() to evaluate each move.
     */
    public TablutMove minimaxDecision(List<TablutMove> moves, TablutBoardState boardState) {
    	double[] moveValue = new double [moves.size()];
    	    	    	
    	/*
    	 * A sort of iterative deepening, where we increase the depth of the search as the game
    	 * progresses, since we can presume there are less move possibilities later in games
    	 * due to piece capture.
    	 */
    	int currentTurn = boardState.getTurnNumber();
    	int maxDepth = 3;

    	if (currentTurn > 25) {
    		maxDepth = 4;
    	} else if (currentTurn > 60) {
    		maxDepth = 5;
    	}
    	
    	int curMoveIdx = 0;
    	for (TablutMove curMove: moves) {
    		// Clone the board state and apply the move to obtain the new game state and evaluate it using minimax
            TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
            clonedBoardState.processMove(curMove); // apply the operator o and obtain the new game state s.
            
            if (clonedBoardState.getWinner() == player_id) {
            	return curMove;
            }
            
            // start with alpha & beta = -10K/10K and initial depth 1
            moveValue[curMoveIdx] = minimaxValue(clonedBoardState, -10000, 10000, 1, maxDepth); // Value[o] = MinimaxValue(s)
            
            curMoveIdx++;
    	}
    	
    	//return the operator with the highest value Value[o]
    	return moves.get(getHighestValueMove(moveValue));
    }
    
    /**
     * Helper method to iterate the move values array and return
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
     * Helper method to determine if a state is terminal.
     */
    public boolean isTerminal(TablutBoardState boardState) {
    	return boardState.gameOver();
    }
    
    /**
     * This implementation of the MiniMaxValue() method shown in class
     * is a hybrid between the pseudocode of minimax and the code for alpha beta pruning,
     * as this implementation contains alpha beta pruning.
     */
    public double minimaxValue(TablutBoardState boardState, double alpha, double beta, int depth, int maxDepth) {
    	if (depth == maxDepth) { // if we've reached the maximum decided depth, evaluate and return this state
    		return evaluation(boardState);
    	}
    	
    	// if isTerminal(s), return Utility(s) based on board state
    	if (isTerminal(boardState)) {
    		int winner = boardState.getWinner();
    		if (winner == player_id) {
    			return 5000;
    		} else if (winner == Math.abs(player_id - 1)) {
    			return -5000;
    		} else {
    			// in the case of a draw TODO: change this
        		return evaluation(boardState);
    		}
    	} else {
            List<TablutMove> moves = boardState.getAllLegalMoves();
        	double [] stateValues = new double[moves.size()];

    		ArrayList<TablutBoardState> successors = getSuccessors(boardState, moves);
        	
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
     * Retrieves all successor states for a given board state and moveset
     */
    public ArrayList<TablutBoardState> getSuccessors(TablutBoardState boardState, List<TablutMove> moves) {
    	ArrayList<TablutBoardState> successors = new ArrayList<TablutBoardState>();
    	
    	for (TablutMove curMove: moves) {
    		TablutBoardState clonedBoardState = (TablutBoardState) boardState.clone();
    		clonedBoardState.processMove(curMove); // apply the operator o and obtain the new game state s.
    		successors.add(clonedBoardState);
    	}

    	return successors;
    }
    
    /**
     * Computes the evaluation function for given board state.
     * Swedes are the maximizing player, Muscovites are the minimizing player.
     * Need to set what you are at the beginning, and from there you'll know who is the maximizing player (you).
     * If you are Muscovites, you do not care about mobility in the state you are evaluating
     * If you are Swedes, you do care about mobility and factor it into the evaluation function
     */
    public double evaluation(TablutBoardState boardState) {
        int opponent = Math.abs(player_id - 1);
        
//        // If the current player is a Swede, higher mobility gives a higher board state
//        // If the current player is a Muscovite, mobility does not affect the board state
//        int mobility = 0;
        
        double oppPieceValue = (double) boardState.getNumberPlayerPieces(opponent);
        double yourPieceValue = (double) boardState.getNumberPlayerPieces(player_id);

        if (player_id == TablutBoardState.SWEDE) {
//        	mobility = boardState.getAllLegalMoves().size();
        	//If you, the max player, are a Swede, you attribute a higher value to the opponent pieces
        	oppPieceValue = oppPieceValue * 1.5;
        } else {
        	//If you're a Muscovite, you attribute a higher value to your pieces
        	yourPieceValue = 1.5 * yourPieceValue;
        }

    	return yourPieceValue - oppPieceValue;// + mobility;
    }
}