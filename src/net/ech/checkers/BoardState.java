//
// BoardState.java
//

package net.ech.checkers;

//
// A compact representation of a checkers board.
//
public class BoardState implements Constants
{
    private int occupiedBits;
    private int southBits;
    private int kingBits;

    public BoardState ()
    {
    }

    public BoardState (BoardState copyMe)
    {
        occupiedBits = copyMe.occupiedBits;
        southBits = copyMe.southBits;
        kingBits = copyMe.kingBits;
    }

    public static BoardState makeNewGame ()
    {
        BoardState newBoard = new BoardState ();
        newBoard.occupiedBits = 0xfff00fff;
        newBoard.southBits = 0xfff;
        return newBoard;
    }

    public int getPlayerAt (int sqIndex)
    {
        int mask = squareIndexToMask (sqIndex);
        return
            ((occupiedBits & mask) == 0)
                ? NULL_PLAYER
                : (((southBits & mask) == 0) ? NORTH : SOUTH);
    }

    public boolean isKingAt (int sqIndex)
    {
        int mask = squareIndexToMask (sqIndex);
        return (kingBits & mask) != 0;
    }

    public void kingMe (int sqIndex)
    {
        kingBits |= squareIndexToMask (sqIndex);
    }

    private static int squareIndexToMask (int sqIndex)
    {
        int shift = 31 - (sqIndex / 2);
        return 1 << shift;
    }

    //
    // Execute a move.
    // This method should not king the piece.  Otherwise, class Rules 
    // will allow a piece to be king-ed mid-hop.
    //
    BoardState executeMove (Move move)
    {
        BoardState newBoard = new BoardState (this);

        // Identify the moving piece.
        int originSquare = move.getOrigin ();
        int originMask = squareIndexToMask (originSquare);
        boolean southMoving = (newBoard.southBits & originMask) != 0;
        boolean kingMoving = (newBoard.kingBits & originMask) != 0;

        // Remove the moving piece from its current position.
        newBoard.occupiedBits &= ~originMask;
        newBoard.southBits &= ~originMask;
        newBoard.kingBits &= ~originMask;

        int targetSquare = 0;   // init to silence compiler warning

        for (int i = 1; i <= move.getLength (); ++i)
        {
            targetSquare = move.getVertex (i);

            if (move.isHop ())
            {
                // Remove hopped pieces.
                int hoppedSquare =
                    Position.between (originSquare, targetSquare);
                int hoppedMask = squareIndexToMask (hoppedSquare);
                newBoard.occupiedBits &= ~hoppedMask;
                newBoard.southBits &= ~hoppedMask;
                newBoard.kingBits &= ~hoppedMask;
            }

            originSquare = targetSquare;
        }

        // Place the moving piece at its new position.
        int targetMask = squareIndexToMask (targetSquare);
        newBoard.occupiedBits |= targetMask;
        if (southMoving)
            newBoard.southBits |= targetMask;
        if (kingMoving)
            newBoard.kingBits |= targetMask;

        return newBoard;
    }

    public int getPieceCount (boolean south, boolean king)
    {
        int bits = occupiedBits;
        bits &= south ? southBits : ~southBits;
        bits &= king ? kingBits : ~0;
        return bitCount (bits);
    }

    private static int bitCount (int bits)
    {
        int[] nibbleBits = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };
        int sum = 0;
        while (bits != 0)
        {
            sum += nibbleBits[bits & 0xf];
            bits >>>= 4;
        }
        return sum;
    }

    public boolean hasPiece (boolean south)
    {
        int bits = occupiedBits;
        bits &= south ? southBits : ~southBits;
        return bits != 0;
    }
}
