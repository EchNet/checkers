//
// BoardApplet.java
//

package net.ech.checkers;

import java.awt.*;
import java.io.*;

/**
 * BoardApplet is an applet that plays checkers.
 */
public class BoardApplet extends java.applet.Applet
    implements Constants
{
    public void init ()
    {
        int level = 1;
        try
        {
            level = Integer.parseInt (getParameter ("autoPlayerLevel"));
        }
        catch (Exception e)
        {
        }

        PrintWriter logWriter = null;
        String logFile = getParameter ("logFile");
        if (logFile != null)
        {
            try
            {
                logWriter =
                    new PrintWriter (new FileOutputStream (logFile, true), true);
            }
            catch (Exception e)
            {
                e.printStackTrace ();
            }
        }

        Model model = new Model ();
        AutoPlayer autoPlayer = new AutoPlayer (model);
        BoardComponent boardComp = new BoardComponent ();

        autoPlayer.setLevel (level);
        autoPlayer.setLogWriter (logWriter);
        model.addModelListener (autoPlayer);
        boardComp.setModel (model);

        setLayout (new GridLayout (1, 1));
        add (boardComp);
    }
}
