package nowhere2gopp.gamelogic.gameIO;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.gamelogic.GamePhase;
import nowhere2gopp.gamelogic.Node;
import nowhere2gopp.gamelogic.SiteColor;

/**
 * This part implements assignment 3 a)
 * "Erstellen Sie eine Klasse, die die Schnittstelle nowhere2gopp.preset.Viewer
 * implementiert."
 * @author Marvin Sommer
 * @version 0.1
 */
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Site;
import nowhere2gopp.preset.SiteSet;
import nowhere2gopp.preset.Status;
import nowhere2gopp.preset.Viewer;

/**
 * This class implements the {@link nowhere2gopp.preset.Viewer Viewer}
 * Interface
 * @author Marvin Sommer
 */
public class BoardViewer implements Viewer, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * GameBoard which should be represented
     */
    private GameBoard board;

    /**
     * Constructor of our Viewer that takes a {@link nowhere2gopp.gamelogic.GameBoard GameBoard}
     * instance and sets it to the Board to be viewed.
     * @param board Board to represent
     */
    public BoardViewer(GameBoard board) {
        this.board = board;
    }

    /**
     * Overrides the getTurn function of the Interface. Returns the PlayerColor of the current turn.
     * @return gets this turns PlayerColor
     */
    @Override
    public PlayerColor getTurn() {
        // enum ist immutable
        return board.getTurn();
    }

    /**
     * Gets the size of the GameBoard
     * @return size of the GameBoard
     */
    @Override
    public int getSize() {
        // primitives don't pass references
        return board.getSize();
    }

    /**
     * Gets the Status of the GameBoard
     * @return Status of the GameBoard
     */
    @Override
    public Status getStatus() {
        return board.getStatus();
    }

    /**
     * Gets the current site of the specified PLayerColor
     * @param  color Playercolor
     * @return       current site of corresponding player
     */
    @Override
    public Site getAgent(final PlayerColor color) {
        Site readme = board.getAgent(color);

        // New instance so we dont pass the reference
        return new Site(readme.getColumn(), readme.getRow());
    }

    /**
     * Returns the Links that are not deleted yet
     * @return non-deleted Links
     */
    @Override
    public Collection<SiteSet>getLinks() {
        Collection<SiteSet> list      = new LinkedList<SiteSet>();
        Collection<SiteSet> boardList = board.getLinks();

        // copy copied elements into a new list so we dont pass the reference
        for (SiteSet link : boardList) {
            list.add(new SiteSet(link));
        }
        return list;
    }

    /**
     * Gets the a copy of PossibleMoves
     * @return returns the PossibleMoves of the current player
     */
    public HashMap<Move, Move>getPossibleMoves() {
        return board.copyMoveMap(board.getPossibleMoves(getTurn()));
    }

    /**
     * Gets the GamePhase
     * @return current GamePhase as ENUM
     */
    public GamePhase getPhase() {
        return board.getGamePhase();
    }

    /**
     * Gets the already played amount of Rounds
     * @return amount of played Rounds
     */
    public int getRounds() {
        return board.getRounds();
    }

    /**
     * Gets the Sites to preview for possible Moves in Phase 2
     * They consist of all Sites with at least one reachable neighboring Site
     * @return  Collection containing valid initial sites to move to in phase 2
     */
    public Collection<Site>getPhaseTwoPreview() {
        Collection<Node> whiteNodes = board.getWhiteSites();
        LinkedList<Site> whiteSites = new LinkedList<Site>();

        for (Node n : whiteNodes) {
            if (n.getNeighbors(SiteColor.WHITE).size() != 0) {
                whiteSites.add(n.getSite());
            }
        }
        return whiteSites;
    }

    /**
     * Returns all Sites that are reachable by the given Site
     *
     * @param   s   Originating Site
     * @return  Sites to preview
     */
    public Collection<Site>getMoveSecondSitePreview(Site s) {
        LinkedList<Node[]> reachableNodes = board.reachableSites(s);
        LinkedList<Site>   toPreview      = new LinkedList<Site>();

        for (Node[] pair : reachableNodes) {
            toPreview.add(pair[1].getSite());
        }
        return toPreview;
    }
}
