//
// Position.java
//

package net.ech.checkers;

/**
 * Class Position knows how to translate from (row, column) to single
 * square index, and back again.
 */
public class Position implements Constants
{
    public int row;
    public int column;

    public Position ()
    {
        this (0, 0);
    }

    /**
     * Construct (row, column) position.
     */
    public Position (int row, int column)
    {
        this.row = row;
        this.column = column;
    }

    /**
     * Construct single square index position.
     */
    public Position (int sqIndex)
    {
        this.row = squareIndexToRow (sqIndex);
        this.column = squareIndexToColumn (sqIndex);
    }

    public static int toSquareIndex (int row, int column)
    {
        return (row * SQUARES_ON_SIDE) + column;
    }

    public static int squareIndexToRow (int sqIndex)
    {
        return sqIndex / SQUARES_ON_SIDE;
    }

    public static int squareIndexToColumn (int sqIndex)
    {
        return sqIndex % SQUARES_ON_SIDE;
    }

    /**
     * Return the index of the square that's in between the two given
     * squares, assuming that they are separated by one square.
     */
    public static int between (int sq1, int sq2)
    {
        int row1 = squareIndexToRow (sq1);
        int column1 = squareIndexToColumn (sq1);
        int row2 = squareIndexToRow (sq2);
        int column2 = squareIndexToColumn (sq2);
        return toSquareIndex ((row1 + row2) / 2, (column1 + column2) / 2);
    }
}
