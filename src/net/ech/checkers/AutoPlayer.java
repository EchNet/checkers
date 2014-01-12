//
// AutoPlayer.java
//

package net.ech.checkers;

import java.util.*;
import java.io.*;

/**
 * AutoPlayer: an automatic checkers player.
 * I am Jeffrey's nemesis.
 */
public class AutoPlayer implements Constants, Runnable, ModelListener
{
    private Model model;
    private int level = 1;
    private PrintWriter logWriter;

    public AutoPlayer (Model model)
    {
        this.model = model;
    }

    /**
     * Set the difficulty level, equivalent to the lookahead level.
     */
    public void setLevel (int level)
    {
        if (level < 0) level = 0;
        if (level > 10) level = 10;
        this.level = level;
    }

    /**
     * Enable logging by setting a log output stream.
     */
    public void setLogWriter (PrintWriter logWriter)
    {
        this.logWriter = logWriter;
    }

    /**
     * Respond to model change by taking my turn, when it's my turn.
     */
    public void modelChanged (Model model)
    {
        // FOR NOW: always play north.
        if (model.getActivePlayer () == NORTH)
        {
            new Thread (this).start ();
        }
    }

    /**
     * What I do when it's my turn.
     */
    public void run ()
    {
        System.out.println ("Hmmm.");
        Move move = chooseMove ();
        System.out.println ("I'll move... that one.");
        model.executeMove (move);
    }

    /**
     * The logic of choosing a next move automatically begins here.
     */
    private Move chooseMove ()
    {
        // Ask the Rules to find all possible moves.
        GameState gameState = model.copyGameState ();
        Move[] moves = Rules.findMoves (gameState);
        if (moves.length == 0)
            return null;        // should not happen.

        if (logWriter != null)
        {
            logWriter.println ();
            logWriter.println ("Possible moves: " + moves.length);
        }

        // Evaluate each move, choose the best.
        Move bestMove = moves[0];
        if (moves.length > 1)
        {
            int bestScore = scoreMove (gameState, bestMove, 0);
            if (logWriter != null)
            {
                logWriter.println ("1) " + bestMove + " = " + bestScore);
            }
            for (int i = 1; i < moves.length; ++i)
            {
                Move thisMove = moves[i];
                int thisScore = scoreMove (gameState, thisMove, 0);
                if (logWriter != null)
                {
                    logWriter.println ((i + 1) +") " + thisMove + " = " + thisScore);
                }
                if (thisScore > bestScore)
                {
                    bestScore = thisScore;
                    bestMove = thisMove;
                }
            }
        }

        if (logWriter != null)
        {
            logWriter.println ("MOVING " + bestMove);
        }

        return bestMove;
    }

    /**
     * Evaluate a move by looking ahead, up to the maximum lookahead depth.
     */
    private int scoreMove (
        GameState gameState, Move move, int depth)
    {
        // Save current game state.
        int oldActivePlayer = gameState.getActivePlayer ();
        BoardState oldBoardState = gameState.getBoardState ();

        // Postulate the move.
        gameState.executeMove (move);

        int score;
        if (depth == level ||
            gameState.getActivePlayer () == NULL_PLAYER)
        {
            // Can look ahead no further.  Grade the game as it stands.  
            score = scoreGame (gameState, oldActivePlayer == SOUTH);
        }
        else
        {
            // Look ahead, recursively.
            score = findBestScore (gameState, depth + 1) * -1;
        }

        // Restore game state.
        gameState.setActivePlayer (oldActivePlayer);
        gameState.setBoardState (oldBoardState);

        return score;
    }

    /**
     * Grade the game as it stands.  
     * If 'asSouth' is true, grade the game from the point of view of the
     * south player.  Return a number in -1000..1000.
     */
    private static int scoreGame (GameState gameState, boolean asSouth)
    {
        int northPieces = gameState.getPieceCount (NORTH);
        int southPieces = gameState.getPieceCount (SOUTH);

        if (gameState.getActivePlayer () == NULL_PLAYER)
        {
            if ((asSouth && southPieces > 0) || (!asSouth && northPieces > 0))
                return 1000;
        }

        int scoreAsNorth;
        if (northPieces == 0)
            scoreAsNorth = -1000;
        else if (southPieces == 0)
            scoreAsNorth = 1000;
        else
            scoreAsNorth =
                northPieces + gameState.getKingCount (NORTH) 
                - southPieces - gameState.getKingCount (SOUTH);
        return asSouth ? (scoreAsNorth * -1) : scoreAsNorth;
    }

    /**
     * Recursive part of the lookahead algorithm.
     */
    public int findBestScore (GameState gameState, int depth)
    {
        Move[] moves = Rules.findMoves (gameState);
        Move bestMove = null;
        int bestScore = 0;

        for (int i = 0; i < moves.length; ++i)
        {
            Move thisMove = moves[i];
            int thisScore = scoreMove (gameState, thisMove, depth);
            if (bestMove == null || thisScore > bestScore)
            {
                bestScore = thisScore;
                bestMove = thisMove;
            }
        }

        if (bestMove == null)
            throw new IllegalStateException ("look for best move of none");

        return bestScore;
    }
}

