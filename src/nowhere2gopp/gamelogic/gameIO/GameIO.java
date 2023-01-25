package nowhere2gopp.gamelogic.gameIO;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import nowhere2gopp.gamelogic.GamePhase;
import nowhere2gopp.gamelogic.InvalidMoveException;
import nowhere2gopp.gamelogic.gameIO.components.ShapedBtn;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.MoveFormatException;
import nowhere2gopp.preset.MoveType;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Requestable;
import nowhere2gopp.preset.Site;
import nowhere2gopp.preset.SiteSet;
import nowhere2gopp.preset.Status;

/**
 * This class implements the In-/Output Window with clickable polygons for the
 * corresponding game elements and a statusbar.
 *
 * @author Marvin Sommer
 * @version 0.3
 */

public class GameIO implements GameOutput, Requestable {
    /**
     * Default Link color
     */
    final Color C_DEFAULT_LINK = new Color(163, 163, 163);

    /**
     * Default preview Color
     */
    final Color C_PREVIEW = new Color(91, 151, 76);

    /**
     * Default Site color
     */
    final Color C_DEFAULT_SITE = new Color(71, 71, 71);

    /**
     * Default color of Blue sites
     */
    final Color C_PBLUE = new Color(45, 42, 124);

    /**
     * backgroundcolor for pending blue moves
     */
    final Color C_PBLUE_PENDING = new Color(71, 67, 204);

    /**
     * Default color of Red sites
     */
    final Color C_PRED = new Color(124, 45, 51);

    /**
     * backgroundcolor of pending red moves
     */
    final Color C_PRED_PENDING = new Color(209, 76, 86);

    /**
     * Contains the clicked Links of the current request cycle
     */
    private LinkedList<ShapedBtn<SiteSet>> clickedLinks;

    /**
     * Contains the clicked Sites of the current request cycle
     */
    private LinkedList<ShapedBtn<Site>> clickedSites;

    /**
     * Contains the BoardViewer which gives us data about the
     * {@link nowhere2gopp.gamelogic.GameBoard GameBoard}.
     */
    public BoardViewer gamestate;

    /**
     * Contains the game window. Is an internal class.
     */
    private BoardDisplay gameWindow;

    /**
     * HashMap to store possibleMoves in
     */
    private HashMap<Move, Move> possibleMoves;

    /**
     * Object used for snychronization of clicks
     */
    private Object lock = new Object();

    /**
     * last move we updated our GUI with
     */
    private Move lastUpdate;

    /**
     * The collection of our last recolored sites for preview. Used by {@link nowhere2gopp.gamelogic.gameIO.GameIO.BoardDisplay#revertColorSites() revertColorSites} to
     * revert previews.
     */
    private Collection<Site> lastColored;

    /**
     * Constructor which sets the {@link #gamestate Viewer} for this instance
     *
     * @param viewer viewer that links the UI with the needed data
     */
    public GameIO(BoardViewer viewer) {
        setViewer(viewer);
    }

    /**
     * Request function which initiates the construction of a valid Move instance.
     * @return constructed Move instance
     */
    @Override
    public Move request() {
        gameWindow.setOptionalMovesVisible(true);
        showStatus(gamestate.getStatus());
        clickedLinks = new LinkedList<ShapedBtn<SiteSet>>();
        clickedSites = new LinkedList<ShapedBtn<Site>>();
        updateBGC();
        Move result = fetchMove();
        toggleBGC();
        return result;
    }

    /**
     * Reloads the GUI according to GameBoard state. Useful for save/load
     */
    @Override
    public void reload() {
        reset();
        LinkedList<SiteSet> linksToKeep = (LinkedList<SiteSet>) gamestate.getLinks();

        for (ShapedBtn<SiteSet> link : gameWindow.links) {
            // remove all links that are not intact
            if (!linksToKeep.contains(link.getElement()))
                deleteLink(link);
        }
        try {
            // turn the playercolors to the right ones
            ShapedBtn<Site> red = getAgentBtn(PlayerColor.Red);
            ShapedBtn<Site> blue = getAgentBtn(PlayerColor.Blue);
            red.setColor(C_PRED);
            blue.setColor(C_PBLUE);
        } catch (NullPointerException e) {
            // agent sites not yet initialized, no need to do anything
        }
    }

    private ShapedBtn<Site> getAgentBtn(PlayerColor col) {
        return gameWindow.siteMap.get(gamestate.getAgent(col));
    }

    /**
     * fetches the move-string from our Internal class
     * @return move that was parsed from internal class movestring
     */
    private Move fetchMove() {
        possibleMoves = gamestate.getPossibleMoves();
        boolean moveIsValid = false;
        Move result = null;
        String buffer = "";

        while (!moveIsValid) {
            synchronized (lock) {
                // synchronized on lock object, gets notified after valid string is  constructed
                gameWindow.enableClicks();
                try {
                    lock.wait();
                    buffer = gameWindow.getMoveString();
                    result = Move.parse(buffer);

                    if (!possibleMoves.containsKey(result))
                        throw new InvalidMoveException("Move not possible.");
                } catch (MoveFormatException | InterruptedException | InvalidMoveException iex) {
                    showStatus(Status.Illegal);

                    if (iex instanceof MoveFormatException) {
                        System.out.println("Moveformat not valid.");
                    } else if (iex instanceof InterruptedException) {
                        System.out.println("Synchronization error.");
                    } else {
                        System.out.println("Invalid Move.");
                    }

                    // try again until move is valid
                    continue;
                }
                moveIsValid = true;
            }
        }

        // hides surrenderbutton for enemy turn
        gameWindow.setOptionalMovesVisible(false);
        return result;
    }

    /**
     * Updates the backgroundcolor to the corresponding player backgroundcolor
     */
    public void updateBGC() {
        Color turnCol;

        if (gamestate.getTurn() == PlayerColor.Red) {
            turnCol = C_PRED;
        } else {
            turnCol = C_PBLUE;
        }
        gameWindow.topPanel.setBackground(turnCol);
    }

    /**
     * Toggles Backgroundcolor to the one of the opposite player(used for network play)
     */
    public void toggleBGC() {
        Color turnCol;

        if (gameWindow.topPanel.getBackground() == C_PBLUE) {
            turnCol = C_PRED;
        } else {
            turnCol = C_PBLUE;
        }
        gameWindow.topPanel.setBackground(turnCol);
    }

    /**
     * Sets the viewer which delivers the gamedata and constructs a corresponding
     * window.
     *
     * @param viewer viewer which delivers gamedata
     */
    @Override
    public void setViewer(BoardViewer viewer) {
        this.gamestate = viewer;
        gameWindow = new BoardDisplay(gamestate.getSize());
    }

    /**
     * shows all deleted links again and sets the agent sites to the default color
     */
    @Override
    public void reset() {
        for (ShapedBtn<SiteSet> link : gameWindow.links)
            link.setVisible(true);
        try {
            getAgentBtn(PlayerColor.Red).setColor(C_DEFAULT_SITE);
            getAgentBtn(PlayerColor.Blue).setColor(C_DEFAULT_SITE);
        } catch (NullPointerException e) {
            // Agents werent placed yet, no reason to do anything about it
        }
    }

    /**
     * closes the window and exits
     */
    @Override
    public void close() {
        gameWindow.dispose();
        System.exit(0);
    }

    /**
     * Updates the visual gameboard with a corresponding Move and the matching
     * playercolor, is automatically called by {@link #request request} but can be
     * manually called, when playing over the network or using AI-Players, to update
     * the UI accordingly.
     *
     * @param move   Move to execute
     * @param player Player to execute move for
     */
    @Override
    public synchronized void update(final Move move, final PlayerColor player) {
        showStatus(gamestate.getStatus());

        if (lastUpdate != null && lastUpdate.equals(move))
            return;

        lastUpdate = move;
        try {
            // LinkLink Move -> delete 2 Links
            if (move.getType() == MoveType.LinkLink) {
                deleteLink(gameWindow.linkMap.get(move.getOneLink()));
                wait(150);
                deleteLink(gameWindow.linkMap.get(move.getOtherLink()));
            } else if (move.getType() == MoveType.AgentLink) {
                // Agent Link Move get actual Color playercolor from ENUM PlayerColor
                Color playerCol = player == PlayerColor.Red ? C_PRED : C_PBLUE;
                Site oldpos = move.getAgent().getFirst();
                Site moveto = move.getAgent().getSecond();

                if (gamestate.getPhase() == GamePhase.TWO) {
                    // if we are in Phase 2 place it first and  then wait a bit for visibility
                    // reasons
                    gameWindow.siteMap.get(oldpos).setColor(playerCol);
                    wait(150);
                }

                // recolor the old position to default color
                gameWindow.siteMap.get(oldpos).setColor(C_DEFAULT_SITE);
                wait(150);

                // color new position to playercolor
                gameWindow.siteMap.get(moveto).setColor(playerCol);
                SiteSet link = move.getLink();
                wait(150);
                deleteLink(gameWindow.linkMap.get(link));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a Linkbutton (hides it from GUI)
     *
     * @param link Button to be deleted
     */
    private void deleteLink(ShapedBtn<SiteSet> link) {
        link.setVisible(false);
    }

    /**
     * Shows status on the status label using the function provided by the inner
     * class that extends JFrame.
     *
     * @param status Status to show
     */
    @Override
    public void showStatus(Status status) {
        gameWindow.updateStatus(status, gamestate.getPhase());
    }

    /**
     * Internal class containing the JFrame, its elements and their behaviour
     */
    private class BoardDisplay extends JFrame {
        private static final long serialVersionUID = 1L;

        /**
         * Contains our LayeredPane, on which we manually place our elements
         */
        private JLayeredPane panel;

        /**
         * Contains our JPanel, on which we place our status etc.
         */
        private JPanel topPanel;

        /**
         * Contains our status string.
         */
        private JLabel status;

        /**
         * Contains our clickable surrender-move label
         */
        private JLabel surrender;

        /**
         * Contains our Board size n
         */
        private int n;

        /**
         * Contains (n-1)/2
         */
        private int k;

        /**
         * Contains our frames-width
         */
        private int width;

        /**
         * Contains our frame-height
         */
        private int height;

        /**
         * Contains our diameter of sites with a default value of 62
         */
        private int diameter = 62;

        /**
         * Contains our lastDiameter, so we can determine if only moving or repainting
         * with a polygon of different size is necessary
         */
        private int lastDiameter = diameter;

        /**
         * Contains the amount of pixels between the x-position of one Site and the next
         * horizontally
         */
        private int lr_padding;

        /**
         * Contains the amount of pixels between the y-position of one row of sites and
         * the next vertically
         */
        private int td_padding;

        /**
         * Contains the amount of pixels to shift different rows by
         */
        private int sideStepPixels;

        /**
         * Contains a String thats constructed by the buttons to form a valid movestring
         */
        private String parseMe = "";

        /**
         * remembers the number of clicked buttons this turn to determine if the
         * currently clicked button was valid
         */
        private int clickedThisTurn = 0;

        /**
         * Contains the multiplicator for our Line width that depends on our diameter
         * its only half the width of the lines we draw.
         */
        final private double LINE_WIDTH = 0.13;

        /**
         * flag to remember if the board was already constructed for our
         * {@link #placeElements() placeElements} function
         */
        private boolean constructed;

        /**
         * Contains the Shape used for our Sites
         */
        private Shape polySite;

        /**
         * Contains the Shape used for our horizontal links
         */
        private Shape polyHorizontalLink;

        /**
         * Contains the Shape used for our vertical links that are on the left hand side
         * of our sites
         */
        private Shape polyTiltedLeftLink;

        /**
         * Contains the Shape used for our vertical links that are on the right hand
         * side of our sites
         */
        private Shape polyTiltedRightLink;

        /**
         * Contains the number of sites to be created
         */
        private int sitesCount = 0;

        /**
         * Contains the number of links to be created
         */
        private int linkCount = 0;

        /**
         * Contains the scaling ratio we started out with after window construction to
         * compare with resizes
         */
        private double desiredRatio;

        /**
         * Contains our gamePhase starting at index 0 for gamePhase 1
         */
        private int gamePhase = 0;

        /**
         * Link layer for LayeredPane
         */
        private final Integer L_LINK = 100;

        /**
         * Site layer for LayeredPane
         */
        private final Integer L_SITE = 200;

        /**
         * HTML tag start for Status String
         */
        private final static String tagStart = "<html><p style=\"color:WHITE;background-color:BLACK;\">";

        /**
         * HTML tag end for Status String
         */
        private final static String tagEnd = "</p></html>";

        /**
         * Tells us if clicks are enabled for the graphical representation
         */
        private boolean clickEnabled = false;

        /**
         * Array that stores the site buttons
         */
        private ShapedBtn<Site>[] sites;

        /**
         * HashMap that wraps our {@link #sites sites} Array for faster access
         */
        HashMap<Site, ShapedBtn<Site>> siteMap;

        /**
         * Array that stores the link buttons
         */
        private ShapedBtn<SiteSet>[] links;

        /**
         * HashMap that wraps our {@link #links links} Array for faster access
         */
        HashMap<SiteSet, ShapedBtn<SiteSet>> linkMap;

        /**
         * Overloaded constructor which takes the size {@link #n n} of our to be
         * constructed board Initializes and places various needed variables/elements and automatic
         * resizing
         * of gameelements
         */
        public BoardDisplay(int size) {
            constructed = false;
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            initSpacings(size);
            initComponents();
            stackComponents();
            initFrameResizer();
        }

        /**
         * calculate the initial spacings to use for GUI Placement
         * @param size gameboard size
         */
        private void initSpacings(int size) {
            n = size;
            k = (n - 1) / 2;

            // calculate all spacings, and construct necessary polygons
            setDiameter(diameter);

            // now we calculate the width and height of our window
            width = n * lr_padding;
            height = (n + 1) * td_padding;

            // add in some spacing for the title bar,add some room to the bottom
            this.setMinimumSize(new Dimension(350, 300));
            this.setSize(new Dimension(width, height));
            desiredRatio = (double) width / height;
        }

        /**
         * Initializes variables and Panels used by our gamewindow.
         */
        @SuppressWarnings("unchecked")
        private void initComponents() {
            sites = new ShapedBtn[getSiteCount()];
            links = new ShapedBtn[getLinkCount()];
            siteMap = new HashMap<Site, ShapedBtn<Site>>(getSiteCount());
            linkMap = new HashMap<SiteSet, ShapedBtn<SiteSet>>(getLinkCount());
            panel = new JLayeredPane();
            topPanel = new JPanel();
            status = new JLabel();
        }

        /**
         * we stack our previously initialized components inside of each other and specify our outer
         * layout
         */
        private void stackComponents() {
            this.setLayout(new BorderLayout());
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
            topPanel.add(status);
            this.add(panel, BorderLayout.CENTER);
            placeElements();
            this.add(topPanel, BorderLayout.NORTH);
            updateStatus(gamestate.getStatus(), gamestate.getPhase());
            setVisible(true);
            constructed = true;
        }

        /**
         * We add a recalculation of spacings and place our elements into the right positions
         */
        private void initFrameResizer() {
            panel.addComponentListener(new ComponentAdapter() {
                public void componentResized(ComponentEvent e) {
                    calcResize();
                    placeElements();
                }
            });
        }

        /**
         * Calculates the new {@link #diameter diameter} after resize events.
         */
        private void calcResize() {
            width = panel.getWidth();
            height = panel.getHeight();
            double div = 1.5;
            double ratio = (double) width / height;

            if (ratio >= desiredRatio) { // set our diameter according to the current ratio of our panel
                int newDiameter = (int) ((((height * desiredRatio)) / n) / div);
                setDiameter(newDiameter);
            } else {
                int newDiameter = (int) ((width / n) / div);
                setDiameter(newDiameter);
            }
        }

        /**
         * Place our surrenderbutton and puts a glue into the toppanel to position it how we want it
         * to
         */
        private void placeSurrenderBtn() {
            topPanel.add(Box.createHorizontalGlue());
            surrender = new JLabel("<html><p style=\"color:BLACK;background-color:red\">SURRENDER</p></html>");
            addLabelListener(surrender, "surrender");
            surrender.setMaximumSize(surrender.getPreferredSize());
            topPanel.add(surrender);
            setOptionalMovesVisible(false);
        }

        /**
         * sets visibility our our surrender button
         * @param visible visibility flag
         */
        private void setOptionalMovesVisible(boolean visible) {
            surrender.setVisible(visible);
        }

        /**
         * adds an onclick listener to our Sites
         * @param listening Site to add a listener to
         */
        private void addSiteListener(final ShapedBtn<Site> listening) {
            listening.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!clickEnabled)
                        return;

                    if (clickedSites.contains(listening)) // already clicked this turn
                        return;

                    if (gamePhase == 0) // wrong gamephase
                        return;

                    if (gamePhase == 1) {
                        if (clickedThisTurn == 2) // only 2 sites per turn in phase 2
                            return;

                        String site = listening.getElementString();

                        if (clickedThisTurn == 0) { // first clicked site in phase 2
                            parseMe += site + ",";
                            revertColorSites();
                            previewSecondPossibleSite(Site.parse(site));
                        } else { // second clicked site in phase 2
                            revertColorSites();
                            parseMe += listening.getElementString() + ")+";
                            toggleLinkPreview();
                        }
                        clickedSites.add(listening);
                    } else { // phase 3
                        if (clickedThisTurn != 0) // only one click on a site in phase 3 allowed
                            return;

                        parseMe += listening.getElementString() + ")+";
                        revertColorSites();
                        clickedSites.add(listening);
                        toggleLinkPreview();
                    }
                    clickedThisTurn++; // successful click, so increase our counter
                }
            });
        }

        /**
         * init our sites
         * @param n site number to initialize
         * @param x site x game-coordinate
         * @param y site y game-coordinate
         */
        private void initSite(final int n, int x, int y) {
            sites[n] = new ShapedBtn<Site>(polySite, diameter, C_DEFAULT_SITE);
            sites[n].setElement(new Site(x, y));
            siteMap.put(sites[n].getElement(), sites[n]);
            panel.add(sites[n], L_SITE);
            sites[n].setText(x + "|" + y);
            addSiteListener(sites[n]);
        }

        /**
         * place links according to parameters, initializes it if our board variables werent
         * constructed
         * yet
         * @param n     link number to place
         * @param w     link width
         * @param h     link height
         * @param xcord x-game-coordinate of base site
         * @param ycord y-game-coordinate of base site
         * @param poly  polygon to use for our button
         * @param xpos  x position on our panel
         * @param ypos  y position on our panel
         */
        private void placeLink(final int n, int w, int h, int xcord, int ycord, Shape poly, int xpos, int ypos) {
            if (!constructed) {
                initLink(n, w, h, xcord, ycord, poly);
            } else {
                if (lastDiameter != diameter)
                    links[n].setShape(poly, w, h);
            }
            links[n].setBounds(xpos, ypos, w, h);
        }

        /**
         * initialized our link according
         * @param n     link number to initialize
         * @param w     width of our link
         * @param h     height of our link
         * @param xcord x-game-coordinate of base site
         * @param ycord y-game-coordinate of base site
         * @param poly  polygon to use for our button
         */
        private void initLink(final int n, int w, int h, int xcord, int ycord, Shape poly) {
            links[n] = new ShapedBtn<SiteSet>(poly, w, h, C_DEFAULT_LINK);

            // horizontal link
            if (poly.equals(polyHorizontalLink))
                links[n].setElement(new SiteSet(new Site(xcord, ycord), new Site(xcord + 1, ycord)));

            else if (poly.equals(polyTiltedLeftLink))
                links[n].setElement(new SiteSet(new Site(xcord, ycord), new Site(xcord, ycord + 1)));

            // left tilted link
            else
                links[n].setElement(new SiteSet(new Site(xcord, ycord), new Site(xcord + 1, ycord + 1)));

            // right tilted link
            // put into linkmap
            linkMap.put(links[n].getElement(), links[n]);
            panel.add(links[n], L_LINK);
            addLinkListener(links[n]);
        }

        /**
         * Creates all visual gameelements by calling the corresponding init methods if the board
         * wasnt
         * already constructed and
         * places/scales them accordingly after that and on subsequent calls.
         * Sites are placed starting from the bottom row up. Sites normally have an upper left,
         * upper
         * right and horizontal link(to the right neighbor),
         * A Link is placed using the center of its originating Site and the center of its secondary
         * Site as reference points.
         * The O represents the originating Site:
         * \ /
         *  O _
         * Exceptions to this based on the desired board shape are detected and skipped, that
         * includes:
         * <ul>
         * <li>Right border horizontal links</li>
         * <li>Top border upper left/right links</li>
         * <li>Left border upper left links above the center row</li>
         * </ul>
         * They are places one Layer behind our constructed Sites.
         */
        private void placeElements() {
            if (!constructed)
                placeSurrenderBtn();
            // We start constructing from Site 0|0
            int xcord = 0, ycord = 0;
            // Site 0|0 xPosition
            int xstart = ((lr_padding - diameter) / 2) + k * sideStepPixels + width / 2 - (int) (lr_padding * (k + 0.5));
            // Site 0|0 yPosition
            int ystart = (int) (td_padding * (n - 0.5));
            // Variables to modify on row internal
            // iterations, xstart and ystart are used
            // to  calculate next column starting
            // position more easily
            int x = xstart, y = ystart;
            // maximum Sites to put into a row and the
            // variable to save the current site
            // number in row
            int maxInRow = n - k, countInRow = 0;
            // number of additional sites in next row,
            // is changed to -1 as soon as middle row
            // was reached
            int maxRowStep = 1;
            // index of link that is being modified
            int initLinkN = 0;
            // variable that saves if we already have reached the middle row
            boolean middle = false;
            // new starting xcord if middle row was
            // reached (from x=0 to x=1 and so on)
            int stepOverMiddle = 0;


            // --------------------- Init/Place Sites and Links
            for (int i = 0; i < getSiteCount(); i++) {
                if (countInRow < maxInRow) {
                    if (!constructed) {
                        initSite(i, xcord, ycord);
                    } else {
                        if (lastDiameter != diameter)
                            sites[i].setShape(polySite, diameter);
                    }
                    countInRow++;
                    sites[i].setBounds(x, y, diameter, diameter);

                    // variables to save the wanted positions on the panel and the
                    // wanted dimensions of the ShapedBtns
                    int xpos, ypos, w, h;

                    // ------------------------- Horizontal Link Button Placement
                    if (countInRow < maxInRow) {
                        xpos = x;
                        ypos = y;
                        w = lr_padding + diameter / 2;
                        h = diameter;
                        placeLink(initLinkN++, w, h, xcord, ycord, polyHorizontalLink, xpos, ypos);
                    }

                    // ------------------------- Tilted button common parameters
                    w = sideStepPixels + (int) (diameter * LINE_WIDTH * 2);
                    h = td_padding;
                    ypos = (int) (y - td_padding + diameter / 2 + diameter * LINE_WIDTH);

                    // ------------------------- Right Tilted Link Button Placement
                    if (!middle || ((countInRow < maxInRow) && (maxInRow != k + 1))) {
                        xpos = (int) (x + diameter / 2 - diameter * LINE_WIDTH);
                        placeLink(initLinkN++, w, h, xcord, ycord, polyTiltedRightLink, xpos, ypos);
                    }

                    // ------------------------- Left Tilted Link Button Placement
                    if (!middle || ((maxInRow != k + 1) && (countInRow != 1))) {
                        xpos = x - (int) (diameter * LINE_WIDTH * 2);
                        placeLink(initLinkN++, w, h, xcord, ycord, polyTiltedLeftLink, xpos, ypos);
                    }

                    // ------------------------- Set x correlated variables for next loop
                    xcord++;
                    x += lr_padding;

                    // ------------------------- Row transition
                } else { // New starting positions for hexagonal row Placement
                    countInRow = 0;

                    if (!middle) { // Since we start from the bottom if we haven't reached the
                                   // middle part yet, we have to substract our xstart position
                        xstart -= sideStepPixels;
                        x = xstart;
                        xcord = 0;
                    } else {
                        xstart += sideStepPixels; // if we have reached the middle part we
                                                  // have to add it
                        x = xstart;
                        stepOverMiddle++;
                        xcord = stepOverMiddle; // from the middle upwards we increase our
                                                // starting xcoord
                    }
                    maxInRow += maxRowStep;

                    if (maxInRow == n) {
                        maxRowStep = -1;
                        middle = true;
                    }
                    y -= td_padding; // we always go one row up since
                                     // we started from the buttom left
                    ycord++;
                    i--; // one iteration of our loop handled the new placements of
                    // elements,we decrease i so our Button placement logic fires
                    // on the same i
                }
            }
        }

        /**
         * Adds a Label to our panel and sets a corresponding action listener for move
         * construction, attaches the wanted string to the action (surrender, but other predefined
         * moves
         * would be possible)
         * @param label JLabel to add listener to
         * @param name  parsable string to attach
         */
        private void addLabelListener(JLabel label, String name) {
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    synchronized (lock) {
                        if (!clickEnabled)
                            return;

                        parseMe = name;
                        lock.notify();
                    }
                }
            });
        }

        /**
         * Adds the corresponding actionlistener to a visual link element.
         *
         * @param listeningLink link to add the listener to
         */
        private void addLinkListener(ShapedBtn<SiteSet> listeningLink) {
            listeningLink.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!clickEnabled)
                        return;

                    if (clickedLinks.contains(listeningLink)) // only clickable once per turn
                        return;

                    synchronized (lock) {
                        if (gamePhase == 0) {
                            if (clickedThisTurn == 0) { // If its our first click in a LinkLink
                                                        // Move, we add the corresponding String and
                                                        // add a + (formatting of Moves)
                                parseMe += listeningLink.getElementString() + "+";
                                clickedLinks.add(listeningLink);
                                clickedThisTurn++;
                                linkMap.get(SiteSet.parse(listeningLink.getElementString())).setColor(C_DEFAULT_LINK);
                                return;
                            } else {
                                parseMe += listeningLink.getElementString();
                            }
                        } else if (gamePhase == 1) {
                            // in other gamephases we have to click in the right orderor nothing happens
                            if (clickedThisTurn != 2)
                                return;

                            parseMe += listeningLink.getElementString();
                        } else {
                            if (clickedThisTurn != 1)
                                return;

                            parseMe += listeningLink.getElementString();
                        }

                        // we keep the clicked Elements in mind so we dont construct invalid Moves
                        clickedLinks.add(listeningLink);
                        toggleLinkPreview();
                        lock.notify();
                    }
                    return;
                }
            });
        }

        /**
         * Enables Input and prepares {@link #parseMe parseMe} String according to
         * gamephase, previews possible Moves
         */
        private void enableClicks() {
            // set clickable for buttons
            clickEnabled = true;

            if (gamePhase == 1) {
                // initial agent placement agentlink
                parseMe = "(";
                previewWhitePossibleSites();
            }

            if (gamePhase == 2) {
                // already placed
                parseMe = "(" + gamestate.getAgent(gamestate.getTurn()) + ",";

                // agent link move
                previewSecondPossibleSite(gamestate.getAgent(gamestate.getTurn()));
            } else if (gamePhase == 0) {
                // linklink move
                parseMe = "";
                toggleLinkPreview();
            }
            clickedThisTurn = 0;
        }

        /**
         * Colors all Sites that can be used for the generation of a possible move to a preview
         * color.
         * Only to be used in phase 2, for the initial placement.
         */
        private void previewWhitePossibleSites() {
            Collection<Site> whites = gamestate.getPhaseTwoPreview();
            colorSites(whites, C_PREVIEW);
        }

        /**
         * Previews all Sites we can move to from our current position s
         * @param s current position
         */
        private void previewSecondPossibleSite(Site s) {
            Collection<Site> whites = gamestate.getMoveSecondSitePreview(s);
            colorSites(whites, C_PREVIEW);
        }

        /**
         * Colors the Sites in a Collection according to the given color
         * @param colorUs array containing sites of the {@link #sites} buttons to recolor
         * @param color color to paint our buttons in
         */
        private void colorSites(Collection<Site> colorUs, Color color) {
            for (Site s : colorUs) {
                siteMap.get(s).setColor(color);
            }
            lastColored = colorUs;
        }

        /**
         * Toggles the color of all visible links to indicate that they are clickable for move
         * generation.
         */
        private void toggleLinkPreview() {
            for (ShapedBtn<SiteSet> link : links) {
                if (link.isVisible()) {
                    if (link.getColor() == C_PREVIEW)
                        link.setColor(C_DEFAULT_LINK);
                    else
                        link.setColor(C_PREVIEW);
                }
            }
        }

        /**
         * Reverts the most recent previewed site colors
         */
        private void revertColorSites() {
            for (Site s : lastColored) {
                siteMap.get(s).setColor(C_DEFAULT_SITE);
            }
        }

        /**
         * Disables input and returns the {@link #parseMe parseMe} String that was
         * constructed, is called by
         *
         * @return returns the constructed Movestring
         */
        private String getMoveString() {
            clickEnabled = false;
            return parseMe;
        }

        /**
         * Sets the Diameter and calculates the different spacings resulting from it,
         * also creates shapes matching the new diameter.
         *
         * @param diameter new diameter
         */
        private void setDiameter(int diameter) {
            lastDiameter = this.diameter;
            this.diameter = diameter;
            lr_padding = diameter + (diameter / 2);
            td_padding = diameter + (diameter / 4);
            sideStepPixels = lr_padding / 2;
            createShapes();
        }

        /**
         * x coodinates of our rectangular shapes inside of the button
         */
        private int x1, x2, x3, x4;

        /**
         * y coodinates of our rectangular shapes inside of the button
         */
        private int y1, y2, y3, y4;

        /**
         * Creates shapes matching the current diameter variable.
         */
        private void createShapes() {
            polySite = new Ellipse2D.Float(0, 0, diameter, diameter);
            createHorizontalLink();
            createTiltedLinks();
        }

        /**
         * Creates Polygon for the horizontal links
         */
        private void createHorizontalLink() {
            x1 = x2 = diameter / 2; // From the middle of a site to the next one
            x3 = x4 = diameter / 2 + lr_padding;
            y1 = y4 = diameter / 2 + (int) (diameter * LINE_WIDTH);
            y2 = y3 = diameter / 2 - (int) (diameter * LINE_WIDTH);
            polyHorizontalLink = new Polygon(new int[] { x1, x2, x3, x4 }, new int[] { y1, y3, y3, y4 }, 4);
        }

        /**
         * creates Polygons for the tilted Links
         */
        private void createTiltedLinks() {
            x1 = sideStepPixels;

            // A whole line width between x1 and x2
            x2 = sideStepPixels + (int) (diameter * LINE_WIDTH * 2);

            // also between x3 and x4
            x3 = (int) (diameter * LINE_WIDTH * 2);
            x4 = 0;

            // height is the row height 
            y1 = y2 = td_padding;

            // we start at 0 -> we construct a line
            // from the top left to the bottom right
            y3 = y4 = 0;
            polyTiltedLeftLink = new Polygon(new int[] { x1, x2, x3, x4 }, new int[] { y1, y2, y3, y4 }, 4);

            // swap the corresponding y values and we receive a line from top right to bottom left
            y1 = y2 = 0;
            y3 = y4 = td_padding;
            polyTiltedRightLink = new Polygon(new int[] { x1, x2, x3, x4 }, new int[] { y1, y2, y3, y4 }, 4);
        }

        /**
         * Calculates the amount of sites needed for a board of the current size
         *
         * @return needed sites
         */
        private int getSiteCount() {
            // remember it so we dont have to loop on each call
            if (sitesCount != 0)
                return sitesCount;

            int result = n;
            int i = n - 1;
            int step = result;

            while (i > 0) {
                result += 2 * (step - 1);
                i -= 2;
                step--;
            }
            sitesCount = result;
            return result;
        }

        /**
         * Calculates the amount of links needed for a board of the current size
         *
         * @return needed links
         */
        private int getLinkCount() {
            // remember it so we dont have to loop on each call
            if (linkCount != 0)
                return linkCount;

            int i = k;
            int x = k;
            int result = n - 1;

            while (i > 0) {
                result += 2 * (x);
                x++;
                i--;
            }
            result *= 3;
            linkCount = result;
            return result;
        }

        /**
         * Shows the Status information on our status label and updates the gamephase.
         *
         * @param status status to display
         * @param phase  gamephase
         */
        private void updateStatus(Status status, GamePhase phase) {
            String buffer = gamestate.getTurn() == PlayerColor.Red ? " | Red | " : " | Blue | ";

            buffer += phaseToString(phase);

            if (status == null) {
                this.status.setText("Null");
                return;
            }

            switch (status) {
            case Ok:
                buffer = "OK" + buffer;
                break;

            case RedWin:
                buffer = "Red Wins";
                break;

            case BlueWin:
                buffer = "Blue Wins";
                break;

            case Draw:
                buffer = "Draw";
                break;

            default:
                buffer = "Illegal" + buffer;
                break;
            }
            this.status.setText(tagStart + buffer + tagEnd);
            this.status.setMaximumSize(this.status.getPreferredSize());
        }

        /**
         * Takes a gamephase and returns a matching String for the Statusbar
         * @param  phase Phase to parse
         * @return       String that represents our phase
         */
        private String phaseToString(GamePhase phase) {
            if (phase == GamePhase.ONE) {
                return "Phase 1";
            } else if (phase == GamePhase.TWO) {
                gamePhase = 1;
                return "Phase 2";
            } else {
                gamePhase = 2;
                return "Phase 3";
            }
        }
    }
}
