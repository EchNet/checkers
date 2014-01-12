//
// MoveOriginatesAt.java
//

package net.ech.checkers;

/**
 * A move predicate that evaluates to true if a move originates at 
 * a given square.
 */
public class MoveOriginatesAt implements MovePredicate
{
    private int sqIndex;

    public MoveOriginatesAt (int sqIndex)
    {
        this.sqIndex = sqIndex;
    }

    public boolean test (Move operand)
    {
        return operand.getOrigin () == sqIndex;
    }
}
