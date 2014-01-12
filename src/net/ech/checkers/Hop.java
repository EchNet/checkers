//
// Hop.java
//

package net.ech.checkers;

/**
 * Represents a checkers move (hop).
 * Is immutable.
 */
public class Hop extends Move
{
    private int[] extension;

    public Hop (int origin, int target) 
    {
        super (origin, target);
    }

    public Hop extend (int nextTarget)
    {
        Hop newHop = new Hop (getOrigin (), getVertex (1));
        int oldLength = extension == null ? 0 : extension.length;
        newHop.extension = new int [oldLength + 1];
        for (int i = 0; i < oldLength; ++i)
        {
            newHop.extension[i] = extension[i];
        }
        newHop.extension[oldLength] = nextTarget;
        return newHop;
    }

    public int getLength ()
    {
        return super.getLength () +
            (extension == null ? 0 : extension.length);
    }

    public int getVertex (int index)
    {
        return index <= 1 ? super.getVertex (index) : extension[index - 2];
    }

    public boolean isHop ()
    {
        return true;
    }

    public boolean isExtensionOf (Move that)
    {
        int thatLength = that.getLength ();

        if (getLength () <= thatLength)
            return false;

        for (int i = 0; i <= thatLength; ++i)
        {
            if (that.getVertex (i) != getVertex (i))
                return false;
        }

        return true;
    }

    public String toString ()
    {
        StringBuffer buf = new StringBuffer ();
        buf.append ("hop ");
        buf.append (getOrigin ());
        buf.append ("->");
        buf.append (getVertex (1));
        if (extension != null)
        {
            for (int i = 0; i < extension.length; ++i)
            {
                buf.append ("->");
                buf.append (extension[i]);
            }
        }
        return buf.toString ();
    }
}
