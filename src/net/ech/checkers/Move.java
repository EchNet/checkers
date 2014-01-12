//
// Move.java
//

package net.ech.checkers;

/**
 * Represents a checkers move.
 * Is immutable.
 */
public class Move
{
    private int origin;
    private int target;

    public Move (int origin, int target)
    {
        this.origin = origin;
        this.target = target;
    }

    public int getOrigin ()
    {
        return origin;
    }

    public int getLength ()
    {
        return 1;
    }

    public int getVertex (int index)
    {
        return index == 0 ? origin : target;
    }

    public boolean isHop ()
    {
        return false;
    }

    public boolean isExtensionOf (Move that)
    {
        return false;
    }

    public boolean equals (Object obj)
    {
        if (!(obj instanceof Move))
            return false;

        Move that = (Move) obj;
        if (getLength () != that.getLength ())
            return false;

        for (int i = 0; i <= getLength (); ++i)
        {
            if (getVertex (i) != that.getVertex (i))
                return false;
        }

        return true;
    }

    public String toString ()
    {
        return "move " + origin + "->" + target;
    }
}
