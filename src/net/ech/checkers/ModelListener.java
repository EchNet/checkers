//
// ModelListener.java
//

package net.ech.checkers;

import java.util.*;

/**
 * Interface for notification of model change events.
 */
public interface ModelListener extends java.util.EventListener
{
    public void modelChanged (Model model);
}
