//
// MoveExtends.java
//

package net.ech.checkers;

/**
 * Move predicate that evaluates to true if the argument move extends
 * a specified move (by hopping).
 */
public class MoveExtends implements MovePredicate
{
    private Move baseMove;

    public MoveExtends (Move baseMove)
    {
        this.baseMove = baseMove;
    }

    public boolean test (Move operand)
    {
        return operand.isExtensionOf (baseMove);
    }
}

