//
// GameState.java
//

package net.ech.checkers;

import java.util.*;

/**
 * Encapsulation of checkers game state.
 */
public class GameState implements Constants
{
    private BoardState board = new BoardState ();
    private int activePlayer = NULL_PLAYER;

    public GameState ()
    {
    }

    public GameState (GameState that)
    {
        this.board = that.board;
        this.activePlayer = that.activePlayer;
    }

    public void clear ()
    {
        board = new BoardState ();
        activePlayer = NULL_PLAYER;
    }

    public void restart ()
    {
        board = BoardState.makeNewGame ();
        activePlayer = SOUTH;
    }

    /**
     * Return NORTH or SOUTH if there is a piece at the indicated position.
     * Return NULL_PLAYER if there is no piece at the indicated position,
     * or if the position is invalid.
     */
    public int getPlayerAt (int row, int column)
    {
        return
            validSquare (row, column)
                ? board.getPlayerAt (Position.toSquareIndex (row, column))
                : NULL_PLAYER;
    }

    public int getPlayerAt (int sqIndex)
    {
        return getPlayerAt (
            Position.squareIndexToRow (sqIndex),
            Position.squareIndexToColumn (sqIndex));
    }

    public boolean isKingAt (int row, int column)
    {
        return 
            validSquare (row, column)
                ? board.isKingAt (Position.toSquareIndex (row, column))
                : false;
    }

    public boolean isKingAt (int sqIndex)
    {
        return isKingAt (
            Position.squareIndexToRow (sqIndex),
            Position.squareIndexToColumn (sqIndex));
    }

    public static boolean validSquare (int row, int column)
    {
        return
            row >= 0 && row < SQUARES_ON_SIDE &&
            column >= 0 && column < SQUARES_ON_SIDE &&
            ((row % 2) == 0) == ((column % 2) == 0);
    }

    public int getPieceCount (int player)
    {
        boolean isSouth;
        switch (player)
        {
        case NORTH:
            isSouth = false;
            break;
        case SOUTH:
            isSouth = true;
            break;
        default:
            return 0;
        }
        return board.getPieceCount (isSouth, false);
    }

    public int getKingCount (int player)
    {
        boolean isSouth;
        switch (player)
        {
        case NORTH:
            isSouth = false;
            break;
        case SOUTH:
            isSouth = true;
            break;
        default:
            return 0;
        }
        return board.getPieceCount (isSouth, true);
    }

    public int getActivePlayer ()
    {
        return activePlayer;
    }

    public BoardState getBoardState ()
    {
        return board;
    }

    public void setBoardState (BoardState board)
    {
        this.board = board;
    }

    public void setActivePlayer (int activePlayer)
    {
        this.activePlayer = activePlayer;
    }

    //
    // Execute a move.
    //
    public void executeMove (Move move)
    {
        // Update board state.
        board = board.executeMove (move);

        // Including kinging.
        int target = move.getVertex (move.getLength ());
        if ((activePlayer == NORTH &&
            Position.squareIndexToRow (target) == SQUARES_ON_SIDE - 1) ||
            (activePlayer == SOUTH &&
            Position.squareIndexToRow (target) == 0))
        {
            board.kingMe (target);
        }

        int nextPlayer = activePlayer == NORTH ? SOUTH : NORTH;

        // Detect end of game.

        if (!board.hasPiece (false) || !board.hasPiece (true) ||
            !Rules.canPlay (board, nextPlayer))
        {
            activePlayer = NULL_PLAYER;
        }
        else
        {
            activePlayer = nextPlayer;
        }
    }
}
