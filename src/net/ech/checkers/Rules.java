//
// Rules.java
//

package net.ech.checkers;

import java.util.*;

//
// I know the rules of checkers.
//
public class Rules implements Constants
{
    // Do not instantiate.
    private Rules ()
    {
    }

    /**
     * Return an array of all the legal moves from the given game state.
     */
    public static Move[] findMoves (GameState gameState)
    {
        BoardState board = gameState.getBoardState ();
        int activePlayer = gameState.getActivePlayer ();
 
        List moveList = new LinkedList ();

        findLegalHops (board, activePlayer, moveList);

        if (moveList.isEmpty ())
        {
            // Player can slide a piece only if there are no legal hops.
            findLegalNonHops (board, activePlayer, moveList);
        }

        if (moveList.isEmpty ())
            return null;

        return scramble ((Move[]) moveList.toArray (new Move [moveList.size ()]));
    }

    //
    // Scramble the list of moves so that the auto-player selects among
    // equally ranked plays randomly.
    //
    private static Move[] scramble (Move[] array)
    {
        for (int i = array.length; --i >= 0; )
        {
            int selected = (int) (Math.random () * (i + 1));
            Move temp = array[i];
            array[i] = array[selected];
            array[selected] = temp;
        }

        return array;
    }

    //
    // Add all the legal hop moves to the list.
    //
    private static void findLegalHops (
        BoardState board,
        int activePlayer,
        List moveList)
    {
        for (int row = 0; row < SQUARES_ON_SIDE; ++row)
        {
            for (int column = ((row % 2) == 0) ? 0 : 1;
                column < SQUARES_ON_SIDE; column += 2)
            {
                int sqIndex = Position.toSquareIndex (row, column);
                if (board.getPlayerAt (sqIndex) == activePlayer)
                {
                    HopFinder hopFinder =
                        new HopFinder (board, sqIndex, moveList);
                    hopFinder.find ();
                }
            }
        }
    }

    private static class HopFinder
    {
        private BoardState board;
        private int origin;
        private Hop prevHop;
        private List moveList;
        private int count;

        HopFinder (BoardState board, int origin, List moveList)
        {
            this.board = board;
            this.origin = origin;
            this.moveList = moveList;
        }

        HopFinder (BoardState board, int origin, Hop prevHop, List moveList)
        {
            this (board, origin, moveList);
            this.prevHop = prevHop;
        }

        void find ()
        {
            int player = board.getPlayerAt (origin);
            boolean isKing = board.isKingAt (origin);

            if (isKing || player == SOUTH)
            {
                checkHop (NORTHWEST);
                checkHop (NORTHEAST);
            }
            if (isKing || player == NORTH)
            {
                checkHop (SOUTHWEST);
                checkHop (SOUTHEAST);
            }
        }

        private void checkHop (int direction)
        {
            int target = Rules.checkHop (board, origin, direction);
            if (target >= 0)
            {
                // Check recursively for further hops.
                // As long as further hops are possible, must keep hopping.
                Hop thisHop = new Hop (origin, target);
                Hop wholeHop = 
                    prevHop == null
                        ? thisHop
                        : prevHop.extend (target);
                HopFinder subFinder =
                    new HopFinder (
                        board.executeMove (thisHop),
                        target, wholeHop, moveList);
                subFinder.find ();
                if (subFinder.getCount () == 0)
                {
                    moveList.add (wholeHop);
                    ++count;
                }
            }
        }

        public int getCount ()
        {
            return count;
        }
    }

    private static int checkHop (BoardState board, int origin, int direction)
    {
        int intermed = adjacentSquare (origin, direction);
        if (intermed >= 0)
        {
            int hopper = board.getPlayerAt (origin);
            int hoppable = hopper == NORTH ? SOUTH : NORTH;
            if (board.getPlayerAt (intermed) == hoppable)
            {
                int target = adjacentSquare (intermed, direction);
                if (target >= 0 &&
                    board.getPlayerAt (target) == NULL_PLAYER)
                {
                    return target;
                }
            }
        }
        return -1;
    }

    private static void findLegalNonHops (
        BoardState board,
        int activePlayer,
        List moveList)
    {
        for (int row = 0; row < SQUARES_ON_SIDE; ++row)
        {
            for (int column = ((row % 2) == 0) ? 0 : 1;
                column < SQUARES_ON_SIDE; column += 2)
            {
                int sqIndex = Position.toSquareIndex (row, column);
                if (board.getPlayerAt (sqIndex) == activePlayer)
                {
                    boolean isKing = board.isKingAt (sqIndex);
                    if (isKing || activePlayer == SOUTH)
                    {
                        checkNonHop (board, sqIndex, NORTHWEST, moveList);
                        checkNonHop (board, sqIndex, NORTHEAST, moveList);
                    }
                    if (isKing || activePlayer == 2)
                    {
                        checkNonHop (board, sqIndex, SOUTHWEST, moveList);
                        checkNonHop (board, sqIndex, SOUTHEAST, moveList);
                    }
                }
            }
        }
    }

    private static void checkNonHop (
        BoardState board, int origin, int direction, List moveList)
    {
        Move move = checkNonHop (board, origin, direction);
        if (move != null)
        {
            moveList.add (move);
        }
    }

    private static Move checkNonHop (
        BoardState board, int origin, int direction)
    {
        int target = adjacentSquare (origin, direction);
        if (target >= 0 && board.getPlayerAt (target) == 0)
        {
            return new Move (origin, target);
        }
        return null;
    }

    private final static int NORTHWEST = 0;
    private final static int NORTHEAST = 1;
    private final static int SOUTHWEST = 2;
    private final static int SOUTHEAST = 3;

    private static int adjacentSquare (int originSqIndex, int direction)
    {
        int row = Position.squareIndexToRow (originSqIndex);
        int column = Position.squareIndexToColumn (originSqIndex);

        switch (direction)
        {
        case NORTHWEST:
        case NORTHEAST:
            row -= 1;
            if (row < 0)
                return -1;
            break;
        case SOUTHWEST:
        case SOUTHEAST:
            row += 1;
            if (row >= SQUARES_ON_SIDE)
                return -1;
        }

        switch (direction)
        {
        case NORTHWEST:
        case SOUTHWEST:
            column -= 1;
            if (column < 0)
                return -1;
            break;
        case NORTHEAST:
        case SOUTHEAST:
            column += 1;
            if (column >= SQUARES_ON_SIDE)
                return -1;
        }

        return Position.toSquareIndex (row, column);
    }

    /**
     * Return true if the player has at least one legal move.
     */
    public static boolean canPlay (BoardState board, int player)
    {
        for (int row = 0; row < SQUARES_ON_SIDE; ++row)
        {
            for (int column = ((row % 2) == 0) ? 0 : 1;
                column < SQUARES_ON_SIDE; column += 2)
            {
                int sqIndex = Position.toSquareIndex (row, column);
                if (board.getPlayerAt (sqIndex) == player)
                {
                    boolean isKing = board.isKingAt (sqIndex);

                    if (isKing || player == SOUTH)
                    {
                        if (checkNonHop (board, sqIndex, NORTHWEST) != null ||
                            checkNonHop (board, sqIndex, NORTHEAST) != null ||
                            checkHop (board, sqIndex, NORTHWEST) >= 0 ||
                            checkHop (board, sqIndex, NORTHEAST) >= 0)
                        {
                            return true;
                        }
                    }
                    if (isKing || player == NORTH)
                    {
                        if (checkNonHop (board, sqIndex, SOUTHWEST) != null ||
                            checkNonHop (board, sqIndex, SOUTHEAST) != null ||
                            checkHop (board, sqIndex, SOUTHWEST) >= 0 ||
                            checkHop (board, sqIndex, SOUTHEAST) >= 0)
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
