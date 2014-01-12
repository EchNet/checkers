//
// Constants.java
//

package net.ech.checkers;

/**
 * Checkers constants.
 */
public interface Constants
{
    public final static int SQUARES_ON_SIDE = 8;
    public final static int SQUARES_ON_BOARD =
        SQUARES_ON_SIDE * SQUARES_ON_SIDE;
    public final static int ON_SQUARES = SQUARES_ON_BOARD / 2;

    public final static int MIN_SQUARE_INDEX = 0;
    public final static int MAX_SQUARE_INDEX = ON_SQUARES - 1;

    public final static int NULL_PLAYER = 0;
    public final static int SOUTH = 1;  // one player.
    public final static int NORTH = 2;  // the other player.
    public final static int NPLAYERS = 2;

    public final static int STARTING_PIECES_PER_PLAYER = 12;
}
