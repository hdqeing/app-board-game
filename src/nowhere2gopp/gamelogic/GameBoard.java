package nowhere2gopp.gamelogic;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import nowhere2gopp.gamelogic.gameIO.BoardViewer;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.MoveType;
import nowhere2gopp.preset.Playable;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Site;
import nowhere2gopp.preset.SiteSet;
import nowhere2gopp.preset.SiteTuple;
import nowhere2gopp.preset.Status;
import nowhere2gopp.preset.Viewer;

/**
 * GameBoard class that implements the core logic of the Nowhere2gopp game.
 * It is initalized with an integer that determines the size of the Gameboards.
 * The Sites which are the platforms for the agents to jump around on, will be
 * stored in a Wrapper class. The Links that connect the Sites with each other,
 * will be left as SiteSets. Both the Sites and the Links are stored in a seperate
 * HashMaps.
 * @author Benedikt W. Berg
 */
public class GameBoard implements Playable, Serializable {

    /**
     * Size of the Gameboard.
     */
    private int gameboardSize;

    /**
     * Integer value that decides the size of the Gameboard and the count of the
     * rounds of the Frist gamephase .
     */
    private int k;

    /**
     * Count of the elapsed rounds.
     */
    private int rounds;

    /**
     * Stores the amount of rounds that the first Gamephase has.
     */
    private int PhaseOneRounds;

    /**
     * Viewer of this Gameboard
     */
    private Viewer boardViewer;

    /**
     * Indicates the current Position of the Blue Agent.
     */
    private Site blueAgent;

    /**
     * Indicates the current Position of the Red Agent.
     */
    private Site redAgent;

    /**
     * Stores the current Status of the Gameboard
     */
    private Status status;

    /**
     * Stores the PlayerColor whose Turn it is.
     */
    private PlayerColor currentTurn;

    /**
     * Stores the Gamephase that the current game is at.
     */
    private volatile GamePhase currentPhase;

    /**
     * Map of the Nodes that store the Site.
     */
    private HashMap<Site, Node> sites = new HashMap<>();

    /**
     * Map of the Links.
     */
    private HashMap<SiteSet, SiteSet> links = new HashMap<>();

    /**
     * Map of the possible Moves of Phase One.
     */
    private EnumMap<PlayerColor, HashMap<Move, Move> > possibleMoves = new EnumMap<>(PlayerColor.class );

    /**
     * Map of the possible Moves of Red in Gamephase two and three.
     */
    private HashMap<Move, Move> redPossibleMoves = new HashMap<>();

    /**
     * Map of the possible Moves of Blue in Gamephase two and three.
     */
    private HashMap<Move, Move> bluePossibleMoves = new HashMap<>();

    /**
     * {@link nowhere2gopp.preset.Move move} of {@link nowhere2gopp.preset.MoveType#Surrender MoveType surrender}
     */
    private final Move surrender = new Move(MoveType.Surrender);

    /**
     * Serialize ID of the GameBoard.
     */
    private static final long serialVersionUID = 1L;

    // ---------------Konstruktor--------------------------------

    /**
     * Constructor of the GameBoard that is passed a integer k that determines the
     * size of the Gameboard and the amount of rounds in the first Gamephase.
     * The current status, Gamephase, turn and the Viewer are initialized and
     * the links and Sites are created with {@link #createPossibleSites() createPossibleSites} and
     * {@link #createPossibleLinks(int,int) createPossibleLinks(column,row)} respectivly.
     * The possible moves of {@link nowhere2gopp.gamelogic.GamePhase#ONE Gamephase One} are
     * calculated with {@link #calculatePhaseOneMoves()}.
     * @param size Integer that is stored in k and which is used to calculate the size and the amount of rounds of the first GamePhase of this board
     */
    public GameBoard(final int size) {
        if ((1 <= size) && (size <= 5)) {
            rounds = 1;
            k = size;
            gameboardSize = 2 * k + 1;
            PhaseOneRounds = (1 << (k - 1)); // equivalent to 2^(k-1)
            status = Status.Ok;
            currentTurn = PlayerColor.Red;
            currentPhase = GamePhase.ONE;
            createPossibleSites();
            createPossibleLinks(0, 0);
            calculatePhaseOneMoves();
        } else {
            throw new IllegalArgumentException("Given Integer must be 1 <= n <= 5 but it was " + size);
        }
    }

    /**
     * This method creates all possible Sites<br/>
     * <pre>
     *   3-4-5<br/>
     *  2-3-4-4<br/>
     * 1-2-3-3-3<br/>
     *  1-2-2-2<br/>
     *   1-1-1<br/>
     *   <pre/>
     * e.q. Ascii board with n=5=2*k+1 k=2
     * per iteration step the sites(see the ascii exp.) are created{@link #createSite(int, int) createSite(int column, int row)} in nummerologic
     * order.
     * First the 1s than the 2s etc.
     */
    private void createPossibleSites() {
        for (int i = 0; i < gameboardSize; i++) {
                createSite(i, i);
            for (int j = 1; j <= k; j++) {
                createSite(i + j, i);
                createSite(i, i + j);
            }
        }
    }

    /**
     * This Method is called by createPossibleSites to construct the site with the
     * given ints column and row. Than it is added to the Map{@link #sites sites} as part of a Node Object.
     * @param column Integer of the Column of the Site.
     * @param row Integer of the Row of the Site.
     */
    private void createSite(final int column, final int row) {
        Site site = null;
        if ((column < gameboardSize) && (row < gameboardSize)) {
            site = new Site(column, row);
            sites.put(site, new Node(site));
        }
    }

    /**
     * This method creates all possible Links recursivly.
     * It gets two Integers(column,row), which are used to first create a Site and then
     * are incremented to look for neighbours  in {@link #sites sites} with {@link #containsNode(Site) #containsNode(Site site)}.
     * If said neighbour exists, the site and the neighbour are used to create a new Link. This Link ,if it does not already exists{@link
     * #containsLink(SiteSet) containsLink(SiteSet link)};
     * will be put into {@link #links links} and this method calls it self with the incremented column and row.
     * @param column column for Site creation
     * @param row row for Site creation
     */
    private void createPossibleLinks(final int column, final int row) { // TODO 11 zu n
        if ((column < gameboardSize) && (row < gameboardSize)) {
            Site site = new Site(column, row);
            if ((row + 1 < 11) && containsNode(new Site(column, row + 1))
                && !containsLink(new SiteSet(site, new Site(column, row + 1)))) {
                createLink(new SiteSet(site, new Site(column, row + 1)));
                createPossibleLinks(column, row + 1);
            }
            if (((column + 1 < 11) && (row + 1 < 11)) && containsNode(new Site(column + 1, row + 1))
                && !containsLink(new SiteSet(site, new Site(column + 1, row + 1)))) {
                createLink(new SiteSet(site, new Site(column + 1, row + 1)));
                createPossibleLinks(column + 1, row + 1);
            }
            if ((column + 1 < 11) && containsNode(new Site(column + 1, row))
                && !containsLink(new SiteSet(site, new Site(column + 1, row)))) {
                createLink(new SiteSet(site, new Site(column + 1, row)));
                createPossibleLinks(column + 1, row);
            }
        }
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.SiteSet SiteSet} and puts it into
     * {@link #links links} and it establishes the neighbour References between the two sites
     * that make up the {@link nowhere2gopp.preset.SiteSet SiteSet} with {@link #establishNeighbourReferences(Node,Node)
     * establishNeighbourReferences(Node,Node)}.
     * @param link passed {@link nowhere2gopp.preset.SiteSet SiteSet} that is stored in {@link #links links}
     */
    private void createLink(final SiteSet link) {
        Site siteOne = link.getFirst();
        Site siteTwo = link.getSecond();
        if (getLink(link) == null) {
            establishNeighbourReferences(getNode(siteOne), getNode(siteTwo));
            links.put(link, link);
        }
    }

    /**
     * This method is given two Nodes {@link nowhere2gopp.gamelogic.Node Node} which are
     * neighbours. These add each other in there {@link nowhere2gopp.gamelogic.Node#neighbors neighbours}
     * with {@link nowhere2gopp.gamelogic.Node#addNeighbor(Node) addNeighbour(Node)}
     * @param nOne {@link nowhere2gopp.gamelogic.Node Node} neighbour 1
     * @param nTwo {@link nowhere2gopp.gamelogic.Node Node} neighbour 2
     */
    private void establishNeighbourReferences(final Node nOne, final Node nTwo) {
        nOne.addNeighbor(nTwo);
        nTwo.addNeighbor(nOne);
    }


    /**
     * This Methode returns the {@link nowhere2gopp.preset.Viewer Viewer} Object from {@link GameBoard Gameboard}
     * @return {@link #boardViewer boardViewer} variable of the {@link GameBoard Gameboard}
     */
    public Viewer viewer() {
        return new BoardViewer(this);
    }


    /**
     * This Methode is given a {@link nowhere2gopp.preset.Move Move} that is to be made on the {@link GameBoard GameBoard}.
     * If the {@link nowhere2gopp.preset.Move Move} is valid then it is processed with {@link #processMove(Move) processMove(Move move)}.
     * After that the {@link #gamePhaseChanger() gamePhaseChanger} looks if the {@link #rounds rounds} need to be incremented and the
     * {@link #currentPhase Phase} changed. If the the current phase is not {@link nowhere2gopp.gamelogic.GamePhase#ONE One}, then
     * the possible moves need to be updated to contain the moves with Movetype {@link nowhere2gopp.preset.MoveType#AgentLink AgentLink}
     * from {@link #prepAgentLinkMoves() prepAgentLinkMoves}.
     * @param  move                  move to be made on the {@link GameBoard GameBoard}
     * @throws IllegalStateException If the move is not in the possible Moves range this Exception is thrown
     */
    public void make(final Move move) throws IllegalStateException {
        if (getPossibleMoves(currentTurn).containsKey(move)) {
            processMove(move);
        } else {
            status = Status.Illegal;
            throw new InvalidMoveException("This Move " + move + " is not in the Possiblemoves range from " + currentTurn);
        }
        gamePhaseChanger();

        if (currentPhase != GamePhase.ONE) prepAgentLinkMoves();
    }


    /**
     * This Methode is given a {@link nowhere2gopp.preset.Move move} that, depending on the {@link nowhere2gopp.preset.MoveType Movetype},
     * does the following:<br/>
     * {@link nowhere2gopp.preset.MoveType#AgentLink LinkLink}: if {@link nowhere2gopp.gamelogic.GamePhase currentPhase} is ONE do {@link
     * #makeLinkLinkMove(Move) makeLinkLinkMove(move)}.<br/>
     * {@link nowhere2gopp.preset.MoveType#AgentLink AgentLink}: if {@link nowhere2gopp.gamelogic.GamePhase currentPhase} is not ONE do {@link
     * #makeAgentLinkMove(Move) makeLinkLinkMove(move)}.<br/>
     * default: the {@link nowhere2gopp.preset.MoveType Movetype} that was passed was {@link nowhere2gopp.preset.MoveType#AgentLink Surrender} so the
     * player that called this move loses the game.
     * @param move move that was passed from {@link #make(Move) make(move)}
     */
    private void processMove(final Move move) {
        switch (move.getType()) {
        case LinkLink:
            if (currentPhase != GamePhase.ONE)
                throw new InvalidMoveException(
                          "This Move " + move + " is only allowed in GamePhase.One but currentPhase is " + currentPhase);
            makeLinkLinkMove(move);
            break;
        case AgentLink:
            if (currentPhase == GamePhase.ONE)
                throw new InvalidMoveException("This Move " + move + " is only allowed in GamePhase.TWO or GamePhase.THREE but currentPhase is " + currentPhase);
            makeAgentLinkMove(move);
            break;
        default:
            status = currentTurn == PlayerColor.Red ? Status.BlueWin : Status.RedWin;
            break;
        }
    }

    /**
     * This Methode is called towards the end of {@link #make(Move) make()}.<br/>
     * If the {@link #currentTurn currentTurn} is {@link nowhere2gopp.preset.PlayerColor#Blue Blue}, the {@link #rounds rounds} need to be
     * incremented<br/>
     * If the {@link #rounds rounds} are greater than {@link #PhaseOneRounds PhaseOneRounds} and the {@link #currentPhase currentPhase} is {@link
     * nowhere2gopp.gamelogic.GamePhase#ONE One}
     * the {@link #currentPhase currentPhase} is changed to {@link nowhere2gopp.gamelogic.GamePhase#TWO Two}<br/>
     * If the {@link #rounds rounds} are greater than the incremented {@link #PhaseOneRounds PhaseOneRounds} and the {@link #currentPhase
     * currentPhase} is {@link nowhere2gopp.gamelogic.GamePhase#TWO Two}
     * the {@link #currentPhase currentPhase} is changed to {@link nowhere2gopp.gamelogic.GamePhase#THREE Three}<br/>
     * Then with {@link #gameEndCondition(Site) gameEndCondition(Site)} it is checked if a gameending situation is reached.
     * Lastly the Turn is changed.
     */
    private void gamePhaseChanger() {
        if (currentTurn == PlayerColor.Blue)
          rounds++;
        if ((rounds > PhaseOneRounds) && (currentPhase == GamePhase.ONE)) {
            currentPhase = GamePhase.TWO;
        }
        if ((rounds > PhaseOneRounds + 1) && (currentPhase == GamePhase.TWO)) {
            currentPhase = GamePhase.THREE;
        }
        gameEndCondition( redAgent);
        gameEndCondition(blueAgent);
        currentTurn = currentTurn == PlayerColor.Red ? PlayerColor.Blue : PlayerColor.Red;
    }

    /**
     * This Methode is given a {@link nowhere2gopp.preset.Site}, on which an Agent is placed, to check if its corresponding {@link
     * nowhere2gopp.gamelogic.Node Node}
     * has any {@link nowhere2gopp.preset.SiteSet link} with which it can reach another {@link nowhere2gopp.preset.Site}.
     * If this is not the case, the Player whose Agent it is loses the game.
     * @param site {@link nowhere2gopp.preset.Site} on which an Agent is placed.
     */
    private void gameEndCondition(final Site site) {
        if ((currentPhase != GamePhase.ONE) && (site != null)) {
            Node currentNode = getNode(site);
            if (currentNode.getNeighbors(SiteColor.WHITE).size() == 0) {
                status = currentNode.getColor() == SiteColor.RED ? Status.BlueWin : Status.RedWin;
            }
        }
    }


    /**
     * This Methode is given a {@link nowhere2gopp.preset.Move Move} with MoveType {@link nowhere2gopp.preset.MoveType#LinkLink LinkLink} to make
     * on the {@link GameBoard Gameboard}. The neighbour relationships between the {@link nowhere2gopp.gamelogic.Node Nodes}, whose connecting
     * {@link nowhere2gopp.preset.SiteSet links} are being removed, are terminated with {@link #remove2Links(SiteSet, SiteSet) remove2Links(link1,
     * link2)}.
     * The move, and all moves containing one of the {@link nowhere2gopp.preset.SiteSet links} in the move, are then deleted out of the {@link
     * #possibleMoves possibleMoves}.
     * @param move {@link nowhere2gopp.preset.Move} to be made.
     */
    private void makeLinkLinkMove(final Move move) {
        SiteSet linkOne   = move.getOneLink();
        SiteSet linkOther = move.getOtherLink();
        remove2Links(linkOne, linkOther);
        for (Move copyMove : copyMoveMap(possibleMoves.get(PlayerColor.Red)).values()) {
            if (!(copyMove.getType() == MoveType.Surrender)) {
                SiteSet link1 = copyMove.getOneLink();
                SiteSet link2 = copyMove.getOtherLink();
                if (linkOne.equals(link1) || linkOne.equals(link2)) {
                    possibleMoves.get(PlayerColor.Red).remove(copyMove);
                }
                if (linkOther.equals(link1) || linkOther.equals(link2)) {
                    possibleMoves.get(PlayerColor.Red).remove(copyMove);
                }
            }
        }
    }

    /**
     * This Methode is given a {@link nowhere2gopp.preset.SiteSet link} that is then remmoved from the {@link #links links} map.
     * @param link {@link nowhere2gopp.preset.SiteSet link} to be removed from  {@link #links links} map.
     */
    private void removeLink(final SiteSet link) {
        removeNeighborReferences(link);
        links.remove(link);
    }

    /**
     * This Methode is given two {@link nowhere2gopp.preset.SiteSet links} that are then removed from the {@link #links links} map,
     * with {@link #removeLink(SiteSet) removeLink(link)}. The neighbour relationship between the links are terminated.
     * @param linkOne {@link nowhere2gopp.preset.SiteSet link} to be removed from  {@link #links links} map.
     * @param linkTwo {@link nowhere2gopp.preset.SiteSet link} to be removed from  {@link #links links} map.
     */
    private void remove2Links(final SiteSet linkOne, final SiteSet linkTwo) { // var args benutzen
            removeLink(linkOne);
            removeLink(linkTwo);
    }

    /**
     * This Methode is given a {@link nowhere2gopp.preset.SiteSet link} that symbolizes the neighbour relationship between the
     * two {@link nowhere2gopp.preset.Site Sites} making up the link. The corresponding {@link nowhere2gopp.gamelogic.Node Nodes} of these Sites
     * remove thier neighbour relationship of eachother with {@link nowhere2gopp.gamelogic.Node#removeNeighbor(Node) removeNeighbor(Node)}.
     * @param link that symbolizes the neighbour relationships between its sites
     */
    private void removeNeighborReferences(final SiteSet link) {
        Node nOne = getNode(link.getFirst());
        Node nTwo = getNode(link.getSecond());
        nOne.removeNeighbor(nTwo);
        nTwo.removeNeighbor(nOne);
    }

    /**
     * This Methode is given a {@link nowhere2gopp.preset.Move Move} with MoveType {@link nowhere2gopp.preset.MoveType#AgentLink AgentLink} to make
     * on the {@link GameBoard Gameboard}. The neighbour relationship between the {@link nowhere2gopp.gamelogic.Node Nodes}, whose connecting
     * {@link nowhere2gopp.preset.SiteSet link} are being removed, are terminated with {@link #removeLink(SiteSet) removeLink(link)}.
     * The new position of the Agent({@link #redAgent redAgent},{@link #blueAgent blueagent}) on the {@link GameBoard Gameboard}, with
     * {@link #setAgent(PlayerColor, Site) setAgent(currentTurn, destSite)}.
     * @param move move that is to be made on the Gameboard.
     */
    private void makeAgentLinkMove(final Move move) {
        Site destSite = move.getAgent().getSecond();
        SiteSet link  = move.getLink();
        if (containsLink(link)) {
            removeLink(link);
        }
        setAgent(currentTurn, destSite);
    }

    /**
     * This Methode sets the current Position of the Agent oj {@link nowhere2gopp.preset.PlayerColor color} and
     * if the {@link nowhere2gopp.gamelogic.GamePhase currentphase} is THREE, paint the last postion of the Agent {@link
     * nowhere2gopp.gamelogic.SiteColor#WHITE White}.
     * The new {@link nowhere2gopp.gamelogic.Node Site} of the Agent will be colored {@link nowhere2gopp.gamelogic.SiteColor PlayerColor} RED or BLUE.
     * @param color {@link nowhere2gopp.preset.PlayerColor color} of Player whose turn it is
     * @param dest  {@link nowhere2gopp.preset.Site dest} that is the new Position of the Agent
     */
    private void setAgent(final PlayerColor color, final Site dest) { // hier auch Enummap benutzen
        Site current = null;
        if (color == PlayerColor.Red) {
            current = redAgent;
            redAgent = dest;
            getNode(dest).paintRed();
        } else {
            current = blueAgent;
            blueAgent = dest;
            getNode(dest).paintBlue();
        }

        if (currentPhase == GamePhase.THREE) getNode(current).turnWhite();
    }

    /**
     * This Methode is given a {@link nowhere2gopp.preset.Site Site} which is used as origin to determine wich {@link nowhere2gopp.preset.Site Sites}
     * can be reached from said origin. Every tuple of possible origins and destinations is stored in an
     * {@link nowhere2gopp.gamelogic.Node Node[]} Array that is stored in a {@link java.util.LinkedList List} that is returned.
     * @param  site Origin site from which reachable are searched
     * @return      List of {@link nowhere2gopp.gamelogic.Node Node[]} Array that have tuples of origin and reachable Sites
     */
    public LinkedList<Node[]>reachableSites(final Site site) {
        LinkedList<Node[]> list = new LinkedList<>();
        final Node outSet = sites.get(site);
        for (Node dest : sites.values()) {
            if (!outSet.equals(dest) && pathFinder(outSet, dest)) {
                Node[] nodes = { outSet, dest };
                list.add(nodes);
            }
        }
        return list;
    }

    // ---------------calculate LinkLink Moves-----------------------

    /**
     * This Methode calculates all possible Moves of Gamephase {@link nowhere2gopp.gamelogic.GamePhase#ONE One}.
     * For that it goes through all {@link #links links} and constructs {@link nowhere2gopp.preset.MoveType#LinkLink LinkLinkMoves}.
     * These moves, and the {@link nowhere2gopp.preset.MoveType#Surrender Surrender Move}, will be put in {@link #possibleMoves possibleMoves}.
     */
    private void calculatePhaseOneMoves() {
        HashMap<Move, Move> map = new HashMap<>();
        for (SiteSet link1 : links.values()) {
            for (SiteSet link2 : links.values()) {
                if (!link1.equals(link2)) {
                    Move move = new Move(link1, link2);
                    map.put(move, move);
                }
            }
        }
        map.put(surrender, surrender);
        possibleMoves.put(PlayerColor.Red, map);
        possibleMoves.put(PlayerColor.Blue, map);
    }

    // ---------------calculate AgentLink Moves-----------------------

    /**
     * This Methode chooses with how many sites the moves for {@link nowhere2gopp.preset.MoveType#AgentLink AgentLinkMoves} are to be calculated.<br/>
     * If the Gamephase is {@link nowhere2gopp.gamelogic.GamePhase#TWO Two} all possible Origin {@link nowhere2gopp.preset.Site Sites}
     * need to be considered for all possible destinations, meaning for every origin Site find all possible destination Sites where the {@link
     * nowhere2gopp.preset.Site Agent} can be Placed.<br/>
     * If the Gamephase is {@link nowhere2gopp.gamelogic.GamePhase#THREE Three} only the already placed {@link nowhere2gopp.preset.Site Agent}
     * needs to be considered for all possible destinations, meaning for the Agent find all possible destination Sites where the agent can jump
     * to.<br/>
     * To do this {@link #calculateAgentLinkMoves(Node...) calculateAgentLinkMoves(Node...)} is called with variable Arguments(varargs),
     * of either all {@link #sites Sites},if Gamephase is Two, or, if the Gamephase is Three, the already placed Agent.
     * The Map that is generated contains all possible Moves of the Player. These are put in the {@link #possibleMoves possibleMoves}.
     */
    private void prepAgentLinkMoves() {
        HashMap<Move, Move> map = new HashMap<>();
        Node Agent              = currentTurn == PlayerColor.Red ? sites.get(redAgent) : sites.get(blueAgent);
        possibleMoves.remove(currentTurn);
        if (currentPhase == GamePhase.TWO) {
            map = calculateAgentLinkMoves(sites.values().toArray(new Node[sites.size()]));
        } else if (currentPhase == GamePhase.THREE) {
            map = calculateAgentLinkMoves(Agent);
        }
        possibleMoves.put(currentTurn, map);
    }

    /**
     * This Methode is given variable Arguments(varargs) of the Type {@link nowhere2gopp.gamelogic.Node Node} these are used as
     * origin {@link nowhere2gopp.preset.Site Sites}. Then all possible destination Site are found with {@link #pathFinder(Node, Node)
     * pathFinder(origin, destination)} to which origin has a path and
     * creates a new {@link nowhere2gopp.preset.SiteTuple SiteTuple}. This tuple is paired with all still exsisting {@link nowhere2gopp.preset.SiteSet
     * links}
     * to create a new {@link nowhere2gopp.preset.Move move} that is valid given the passed origins.
     * @param  nodes varargs of {@link nowhere2gopp.gamelogic.Node Nodes} to be used as origins
     * @return       {@link java.util.HashMap Hashmap} of created moves
     */
    private HashMap<Move, Move>calculateAgentLinkMoves(final Node...nodes) { // var agrs oder array benutzen
        HashMap<Move, Move> map = new HashMap<>();
        for (Node node1 : nodes) {
            for (Node node2 : sites.values()) {
                if (!(node1.equals(node2)) && pathFinder(node1, node2)) {
                    SiteTuple tuple = new SiteTuple(node1.getSite(), node2.getSite());
                    for (Map.Entry<SiteSet, SiteSet>s : links.entrySet()) {
                        Move move = new Move(tuple, s.getValue());
                        map.put(move, move);
                    }
                }
            }
        }
        map.put(surrender, surrender);
        return map;
    }

    /**
     * This Methode is given an {@link nowhere2gopp.gamelogic.Node origin} and a {@link nowhere2gopp.gamelogic.Node destination} between which
     * a path is searched with {@link #searchPath(ArrayDeque, Node) searchPath(queue, dest)}. If there is a path than true is returned, else
     * false.
     * @param  start {@link nowhere2gopp.gamelogic.Node origin} that is the starting point of the search
     * @param  dest  {@link nowhere2gopp.gamelogic.Node destination} of which a connecting path is searched
     * @return       returns true if a path exists, else false;
     */
    public boolean pathFinder(final Node start, final Node dest) {
        if ((start.getColor() != SiteColor.WHITE) && (currentPhase == GamePhase.TWO)) return false;
        ArrayDeque<Node> queue = new ArrayDeque<>(); // ArrayDeque probieren
        queue.add(start);
        start.paintBlack();
        boolean destExists = searchPath(queue, dest);
        makeSitesWhiteAgain();
        return destExists;
    }

    /**
     * This method is called when the {@link nowhere2gopp.gamelogic.Node Nodes} in {@link #sites} were {@link nowhere2gopp.gamelogic.SiteColor
     * recoloured} in other methods
     * like {@link #pathFinder(Node, Node) pathFinder} or {@link #searchPath(ArrayDeque, Node) searchPath}. All recoloured {@link
     * nowhere2gopp.gamelogic.Node Nodes}
     * with {@link nowhere2gopp.gamelogic.Node#paintWhite() paintWhite()} so that the {@link #sites} can be colored again.
     */
    public void makeSitesWhiteAgain() { // lieber liste benutzen als mit farben
        for (Node node : sites.values()) {
            node.paintWhite();
        }
    }

    /**
     * This method is given a {@link java.util.ArrayDeque queue} and a {@link nowhere2gopp.gamelogic.Node destination}. In the queue there is the
     * origin
     * from which a path to the destination is serached via Breath First Search.
     * @param  queue ArrayDeque with the origin placed in it.
     * @param  dest  destination of the serach
     * @return       true if a path to destination exists, else false
     */
    public boolean searchPath(final ArrayDeque<Node>queue, final Node dest) {
        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            if (currentNode.equals(dest))
              return true;
            LinkedList<Node> neighbours = currentNode.getNeighbors(SiteColor.WHITE);
            for (Node node : neighbours) {
                node.paintBlack();
                queue.add(node);
            }
        }
        return false;
    }

    /**
     * This method can roll back a {@link nowhere2gopp.preset.Move AgentLink Move}. It is given
     * the move that is to be reverted and the {@link java.util.HashMap possibleMoves Map} of the Player
     * did the move. It recreates the link with {@link #createLink(SiteSet) createLink}, decrements the {@link #rounds} if the
     * the turn of {@link nowhere2gopp.preset.PlayerColor the blue player} is to be rolled back and if the rounds are equal or
     * lower than the {@link #PhaseOneRounds phaseOneRounds} the {@link #currentPhase currentPhase} is set to
     * {@link nowhere2gopp.gamelogic.GamePhase#TWO GamePhase Two}.
     * The move is than reverted, the {@link #possibleMoves possibleMoves} are restored with {@link java.util.HashMap possibleMoves Map} and the
     * {@link #status status} is set back to {@link nowhere2gopp.preset.Status#Ok Ok}.
     * @param move move to be reverted
     * @param map  {@link java.util.HashMap possibleMoves Map} how they where befor the move was done
     */
    public void rollBackMove(final Move move, final HashMap<Move, Move>map) {
        SiteSet link = move.getLink();
        createLink(link);
        if (currentTurn == PlayerColor.Red) {
            rounds--;
            if ((rounds == PhaseOneRounds + 1) && (currentPhase == GamePhase.THREE)) {
                currentPhase = GamePhase.TWO;
            }
        }
        currentTurn = currentTurn == PlayerColor.Red ? PlayerColor.Blue : PlayerColor.Red;
        if (GamePhase.THREE == currentPhase) {
            Site dest = move.getAgent().getFirst();
            setAgent(currentTurn, dest);
        } else {
            Site playerSite = getAgent(currentTurn);
            getNode(playerSite).turnWhite();

            if (currentTurn == PlayerColor.Red)
              redAgent = null;
            else
              blueAgent = null;
        }
        possibleMoves.remove(currentTurn);
        possibleMoves.put(currentTurn, map);
        status = Status.Ok;
    }

    /**
     * This method creates a deepCopy of the given move map
     * @param  copyDis map of moves that are to be deep copyed
     * @return         deep copy a map with moves
     */
    public HashMap<Move, Move>copyMoveMap(final HashMap<Move, Move>copyDis) {
        HashMap<Move, Move> map = new HashMap<>();
        for (Move move : copyDis.values()) {
            map.put(move, move);
        }
        return map;
    }

    /**
     * This method returns all {@link nowhere2gopp.gamelogic.Node Nodes} that are currently {@link nowhere2gopp.gamelogic.SiteColor#WHITE white}.
     * @return collection<Node> of all white nodes
     */
    public Collection<Node>getWhiteSites() {
        LinkedList<Node> whiteSites = new LinkedList<Node>();
        for (Node checkMe : sites.values()) {
            if (checkMe.getColor().equals(SiteColor.WHITE)) {
                whiteSites.add(checkMe);
            }
        }
        return whiteSites;
    }

    /**
     * This method returns the {@link #possibleMoves possible Moves} of the specifed {@link PlayerColor PlayerColor}
     * @param  color Player whose possible moves are needed
     * @return possibleMoves for the specified player
     */
    public HashMap<Move, Move>getPossibleMoves(final PlayerColor color) {
        return possibleMoves.get(color);
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.Site Site} and returns a boolean if there is a mapping in {@link #sites Sites} with the site.
     * @param  site key
     * @return      true if key has mapping in {@link #sites Sites}, false if not
     */
    public boolean containsNode(final Site site) {
        return sites.containsKey(site);
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.Site Site} and returns the corresponding {@link nowhere2gopp.gamelogic.Node Node} out of the
     * {@link #sites Sites}.
     * @param  site key to retrieve corresponding {@link nowhere2gopp.gamelogic.Node Node}
     * @return      retrieved Node from {@link #sites Sites}
     */
    public Node getNode(final Site site) {
        return sites.get(site);
    }

    /**
     * This method returns the {@link #sites sites}
     * @return returns {@link java.util.HashMap Hashmap} of {@link nowhere2gopp.gamelogic.Node Nodes}
     */
    public HashMap<Site, Node>getSites() {
        return sites;
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.SiteSet link} and returns a boolean if there is a mapping in {@link #links links} with the
     * link.
     * @param  link key
     * @return      true if key has mapping in {@link #links links}, false if not
     */
    public boolean containsLink(final SiteSet link) {
        return links.containsKey(link);
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.SiteSet link} and returns the corresponding {@link nowhere2gopp.preset.SiteSet link} out of
     * the
     * {@link #links links}.
     * @param  link key to retrieve corresponding {@link nowhere2gopp.preset.SiteSet link}
     * @return      retrieved Node from {@link #links links}
     */
    public SiteSet getLink(final SiteSet link) {
        return links.get(link);
    }

    /**
     * This Method returns the {@link #currentTurn current turn}
     * @return a {@link nowhere2gopp.preset.Status Status} Enum
     */
    public Status getStatus() {
        return status;
    }

    /**
     * This method returns a list of {@link #links links}
     * @return list of {@link nowhere2gopp.preset.SiteSet Sitesets}
     */
    public Collection<SiteSet>getLinks() {
        return links.values();
    }

    /**
     * This method returns the size of the GameBoard
     * @return int of the size of this {@link #GameBoard Gameboard}
     */
    public int getSize() {
        return gameboardSize;
    }

    /**
     * This method is given a {@link nowhere2gopp.preset.PlayerColor PlayerColor} to determine which agent
     * to return
     * @param  color specified PlayerColor
     * @return      agent of specified color
     */
    public Site getAgent(final PlayerColor color) {
        return color == PlayerColor.Red ? redAgent : blueAgent;
    }

    /**
     * This method returns the {@link nowhere2gopp.preset.PlayerColor Player} whose turn it is currently
     * @return {@link nowhere2gopp.preset.PlayerColor PlayerColor}
     */
    public PlayerColor getTurn() {
        return currentTurn;
    }

    /**
     * This method returns the {@link nowhere2gopp.gamelogic.GamePhase currentPhase} of the game
     * @return {@link nowhere2gopp.gamelogic.GamePhase GamePhase}
     */
    public GamePhase getGamePhase() {
        return currentPhase;
    }

    /**
     * This method returns the current round of the game
     * @return {@link #rounds rounds} of the GameBoard
     */
    public int getRounds() {
        return rounds;
    }

    /**
     * This method returns the phaseOneRounds of the game
     * @return {@link #PhaseOneRounds phaseOneRounds} of the GameBoard
     */
    public int getPhaseOneRounds() {
        return PhaseOneRounds;
    }
}
