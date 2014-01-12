//
// BoardComponent.java
//

package net.ech.checkers;

import java.awt.*;
import java.util.*;

/**
 * BoardComponent is a GUI that displays a checkers board and manages
 * user interaction with it.
 */
public class BoardComponent extends Component
    implements Constants, ModelListener, java.awt.event.MouseListener
{
    // Stock colors.
    private final static Color GREEN = new Color (0x22cc22);
    private final static Color BUFF = new Color (0xccbb44);
    private final static Color BLACK = Color.black;
    private final static Color RED = new Color (0xcc2222);

    // Colors in use.
    private Color onSquareColor = GREEN;
    private Color offSquareColor = BUFF;
    private Color gutterColor = BLACK;
    private Color outlineColor = BLACK;
    private Color player1Color = BLACK;
    private Color player2Color = RED;

    // Square display state.
    private final static int NORMAL = 0;        // normal
    private final static int ANCHOR = 1;        // the piece that would move
    private final static int HOT = 2;           // click to continue move
    private final static int GHOST = 3;         // piece that would be jumped
    private final static int PATH = 4;          // position on the hop path
    private final static int AVATAR = 5;        // the moving piece

    private int squareSize = 33;
    private int gutterSize = 1;
    private int outlineSize = 3;

    private Model model;

    // For move selection:
    private int lastSquareClicked = -1;
    private Move[] movesAtSquare = new Move [SQUARES_ON_BOARD];
    private int[] squareState = new int [SQUARES_ON_BOARD];

    private FontMetrics fontMetrics;

    public BoardComponent ()
    {
        addMouseListener (this);
        setFont (new Font ("SansSerif", Font.BOLD, 18)); 
    }

    public void setModel (Model model)
    {
        if (model == this.model)
            return;

        if (this.model != null)
        {
            this.model.removeModelListener (this);
        }

        this.model = model;
        model.addModelListener (this);
    }

    public void paint (Graphics g)
    {
        if (fontMetrics == null)
        {
            fontMetrics = g.getFontMetrics ();
        }

        int sz = getBoardSize ();

        g.setColor (outlineColor);
        g.fillRect (0, 0, sz, sz);

        g.setColor (gutterColor);
        g.fillRect (outlineSize, outlineSize,
            sz - outlineSize, sz - outlineSize);

        int fullSquareSize = squareSize + gutterSize;

        for (int row = 0; row < SQUARES_ON_SIDE; ++row)
        {
            for (int col = 0; col < SQUARES_ON_SIDE; ++col)
            {
                int x = outlineSize + (col * fullSquareSize);
                int y = outlineSize + (row * fullSquareSize);

                boolean on = (row + col) % 2 == 0;
                g.setColor (on ? onSquareColor : offSquareColor);

                int sqIndex = Position.toSquareIndex (row, col);
                switch (squareState[sqIndex])
                {
                case NORMAL:
                    break;
                case HOT:
                    g.setXORMode (Color.white);
                    break;
                case ANCHOR:
                    g.setXORMode (onSquareColor);
                    break;
                }

                g.fillRect (x, y, squareSize, squareSize);
                if (on && model != null)
                {
                    int player = model.getPlayerAt (row, col); 
                    if (player != 0)
                    {
                        g.setColor (
                            Math.abs (player) == SOUTH
                                ? player1Color : player2Color);
                        g.fillOval (x + 2, y + 2,
                            squareSize - 4, squareSize - 4);

                        if (model.isKingAt (row, col))
                        {
                            g.setColor (Color.white);
                            centerText (g, "K", x, y, squareSize, squareSize);
                        }
                    }
                }

                g.setPaintMode ();
            }
        }
    }

    public void centerText (
        Graphics g,
        String text,
        int x, int y, int width, int height)
    {
        x += (width - fontMetrics.stringWidth (text)) / 2;
        y += (height - fontMetrics.getHeight ()) / 2;
        y += fontMetrics.getAscent ();
        g.drawString (text, x, y);
    }

    public int getBoardSize ()
    {
        return
            (squareSize * SQUARES_ON_SIDE) +
            (gutterSize * (SQUARES_ON_SIDE - 1)) +
            (outlineSize * 2);
    }

    public Dimension getPreferredSize ()
    {
        int size = getBoardSize ();
        return new Dimension (size, size);
    }

    public void mouseClicked (java.awt.event.MouseEvent e)
    {
        if (model != null && model.getActivePlayer () == NULL_PLAYER)
        {
            model.restart ();
        }
    }

    public void mousePressed (java.awt.event.MouseEvent e)
    {
        if (model == null || model.getActivePlayer () != SOUTH)
            return;

        // Find in which square the click landed.
        int sqIndex = findSquareIndex (e.getX (), e.getY ()); 
        if (sqIndex == lastSquareClicked)
        {
            // Repeat click.  No change.
            return;
        }

        boolean active = false;
        if (sqIndex >= 0)
        {
            // Is the square a target of the currently selected piece?
            if (movesAtSquare[sqIndex] != null)
            {
                continueMoveSequence (sqIndex);
                active = true;
            }
            // Is there are moveable piece in this square?
            else if (model.getPlayerAt (sqIndex) == SOUTH)
            {
                if (selectPieceToMove (sqIndex))
                {
                    active = true;
                }
            }
        }

        // Click in inactive area.
        if (!active && clearSquareBuffs ())
        {
            repaint ();
        }

        lastSquareClicked = sqIndex;
    }

    private boolean selectPieceToMove (int sqIndex)
    {
        MoveIterator mit =
            model.iterateMoves (new MoveOriginatesAt (sqIndex));
        if (!mit.hasNext ())
        {
            return false;
        }

        clearSquareBuffs ();
        squareState[sqIndex] = ANCHOR;

        while (mit.hasNext ())
        {
            Move move = mit.getNext ();
            int targetSquare = move.getVertex (1);
            if (move.getLength () > 1)
            {
                move = new Hop (move.getOrigin (), targetSquare);
            }
            movesAtSquare[targetSquare] = move;
            squareState[targetSquare] = HOT;
        }

        repaint ();
        return true;
    }

    private void continueMoveSequence (int sqIndex)
    {
        // Continue a move sequence.
        Move selectedMove = movesAtSquare[sqIndex];

        // Is this the last leg of the sequence?
        MoveIterator mit =
            model.iterateMoves (new MoveExtends (selectedMove));
        if (!mit.hasNext ())
        {
            // Move completed!
            model.executeMove (selectedMove);
        }
        else
        {
            // Offer continuation.  It can only be a hop extension.
            Hop selectedHop = (Hop) selectedMove;
            clearSquareBuffs ();
            squareState[selectedHop.getOrigin ()] = ANCHOR;

            int selectedLen = selectedHop.getLength ();
            for (int i = 1; i <= selectedLen; ++i)
            {
                squareState[selectedHop.getVertex (i)] = PATH;
            }

            while (mit.hasNext ())
            {
                Move fullHop = mit.getNext ();
                int nextSquareToClick = 
                    fullHop.getVertex (selectedLen + 1);
                movesAtSquare[nextSquareToClick] = 
                    selectedHop.extend (nextSquareToClick);
                squareState[nextSquareToClick] = HOT;
            }

            repaint (); // Model has not changed, only buffs.
        }
    }

    private boolean clearSquareBuffs ()
    {
        boolean cleared = false;

        for (int i = 0; i < SQUARES_ON_BOARD; ++i)
        {
            cleared |= movesAtSquare[i] != null;
            cleared |= squareState[i] != 0;
            movesAtSquare[i] = null;
            squareState[i] = NORMAL;
        }

        return cleared;
    }

    //
    // Translate pixel position into square index.  Return negative value
    // if pixel position does not fall into a square, or if the square is
    // non-playable.  Otherwise, return the value (row * SQUARES) + column.
    //
    private int findSquareIndex (int mx, int my)
    {
        int fullSquareSize = squareSize + gutterSize;

        mx -= outlineSize;
        int column = mx / fullSquareSize;
        if (column < 0 || column >= SQUARES_ON_SIDE)
            return -1;
        if ((mx % fullSquareSize) >= squareSize)
            return -1;

        my -= outlineSize;
        int row = my / fullSquareSize;
        if (row < 0 || row >= SQUARES_ON_SIDE)
            return -1;
        if ((my % fullSquareSize) >= squareSize)
            return -1;

        return Position.toSquareIndex (row, column);
    }

    // Duncares.
    public void mouseReleased (java.awt.event.MouseEvent e) {}
    public void mouseExited (java.awt.event.MouseEvent e) {}
    public void mouseEntered (java.awt.event.MouseEvent e) {}

    public void modelChanged (Model model)
    {
        lastSquareClicked = -1;
        clearSquareBuffs ();
        repaint ();
    }
}
