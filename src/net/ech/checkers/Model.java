//
// Model.java
//

package net.ech.checkers;

import java.util.*;

/**
 * The model is the keeper of game state.
 * It fires events when game state changes.
 */
public class Model
{
    private GameState gameState = new GameState ();
    private Set listeners = new HashSet ();
    private Move[] moves;

    public Model ()
    {
    }

    public void addModelListener (ModelListener listener)
    {
        listeners.add (listener);
    }

    public void removeModelListener (ModelListener listener)
    {
        listeners.remove (listener);
    }

    public void clear ()
    {
        gameState.clear ();
        modelChanged ();
    }

    public void restart ()
    {
        gameState.restart ();
        modelChanged ();
        moves = Rules.findMoves (gameState);
    }

    public int getActivePlayer ()
    {
        return gameState.getActivePlayer ();
    }

    /**
     * Return NORTH or SOUTH if there is a piece at the indicated position.
     * Return NULL_PLAYER if there is no piece at the indicated position,
     * or if the position is invalid.
     */
    public int getPlayerAt (int row, int column)
    {
        return gameState.getPlayerAt (row, column);
    }

    public int getPlayerAt (int sqIndex)
    {
        return gameState.getPlayerAt (sqIndex);
    }

    public boolean isKingAt (int row, int column)
    {
        return gameState.isKingAt (row, column);
    }

    public boolean isKingAt (int sqIndex)
    {
        return gameState.isKingAt (sqIndex);
    }

    //
    // Get the list of all possible moves.
    //
    public MoveIterator iterateMoves ()
    {
        return iterateMoves (new MovePredicate () 
        {
            public boolean test (Move m)
            {
                return true;
            }
        });
    }

    /**
     * Get a list of all moves that evaluate true when the given
     * predicate is applied to them.
     */
    public MoveIterator iterateMoves (final MovePredicate predicate)
    {
        return new MoveIterator ()
        {
            private int moveIndex = 0;

            public boolean hasNext ()
            {
                if (moves != null)
                {
                    for (; moveIndex < moves.length; ++moveIndex)
                    {
                        if (predicate.test (moves[moveIndex]))
                            return true;
                    }
                }

                return false;
            }

            public Move getNext ()
            {
                if (!hasNext ())
                    throw new IllegalStateException ();
                return moves[moveIndex++];
            }
        };
    }

    /**
     * Execute a move.
     */
    public void executeMove (Move move)
    {
        if (!isLegalMove (move))
        {
            throw new IllegalArgumentException (move.toString ());
        }

        gameState.executeMove (move);
        moves = Rules.findMoves (gameState);
        modelChanged ();
    }

    private boolean isLegalMove (Move move)
    {
        if (moves != null)
        {
            for (int i = 0; i < moves.length; ++i)
            {
                if (moves[i].equals (move))
                    return true;
            }
        }
        return false;
    }

    private void modelChanged ()
    {
        ModelListener[] list =
            (ModelListener[]) listeners.toArray (new ModelListener [0]);
        for (int i = 0; i < list.length; ++i)
        {
            list[i].modelChanged (this);
        }
    }

    public GameState copyGameState ()
    {
        return new GameState (gameState);
    }
}
