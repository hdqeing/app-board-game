package nowhere2gopp.gamelogic.player;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.gamelogic.GamePhase;
import nowhere2gopp.gamelogic.Node;
import nowhere2gopp.gamelogic.Serialize;
import nowhere2gopp.gamelogic.SiteColor;
import nowhere2gopp.gamelogic.gameIO.GameIO;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.MoveType;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Site;
import nowhere2gopp.preset.SiteSet;
import nowhere2gopp.preset.SiteTuple;
import nowhere2gopp.preset.Status;

/**
 * This class implements an AI with the simple strategy
 * @author Benedikt W. Berg ¬ Marvin Sommer
 */
public class SimplePlayer extends AbstractPlayer {
    /**
     * The context component of the AI is stored here
     */
    private LinkedList<Node> selfContextComponent = new LinkedList<>();

    /**
     * The context component of the opponent is stored here
     */
    private LinkedList<Node> opponentContextComponent = new LinkedList<>();

    /**
     * The weight of the context component from the AI is stored here
     */
    private int selfSituation;

    /**
     * The weight of the context component from the opponent is stored here
     */
    private int  opponentSituation;

    /**
     * default constructor that calls the {@link nowhere2gopp.gamelogic.player.AbstractPlayer#AbstractPlayer() superconstructer}
     */
    public SimplePlayer() {
        super();
    }

    /**
     * Constructor that calls {@link #SimplePlayer} sets the {@link #gui gui} to represent the gamestate
     * @param gui new {@link #gui gui}
     */
    public SimplePlayer(GameIO gui) {
        this();
        setGUI(gui);
    }

    /**
     * Initialize the SimplePlayer with a fresh {@link #board} and the corresponding {@link nowhere2gopp.preset.PlayerColor}
     * @param  size            Size of the {@link nowhere2gopp.gamelogic.GameBoard}
     * @param  color           {@link nowhere2gopp.preset.PlayerColor} of this Player
     * @throws Exception       of anykind
     * @throws RemoteException when the network play is interrupted
     */
    @Override
    public void init(int size, PlayerColor color) throws Exception, RemoteException {
        super.init(size, color);
        playerWin = getPlayerColor() == PlayerColor.Red ? Status.RedWin : Status.BlueWin;
        enemyWin  = getEnemyColor() == PlayerColor.Red ? Status.RedWin : Status.BlueWin;
    }

    /**
     * This method returns the next {@link nowhere2gopp.preset.Move move} that this
     * player makes next. The moves of the {@link nowhere2gopp.gamelogic.GamePhase Gamephase One} are
     * randomly chosen with {@link #randLinkLinkMove(LinkedList, int) randLinkLinkMove(possMoves, size of possMoves)}.
     * The moves for the other phases are carefully chosen by predicting enemy reactions with {@link #moveMinMax() moveMinMax}.
     * @return next move the AI wants to make
     * @throws Exception       of anykind
     * @throws RemoteException when the network play is interrupted
     */
    protected Move requestMove() throws Exception, RemoteException {
        LinkedList<Move> moves = new LinkedList<Move>(getGameBoard().getPossibleMoves(getGameBoard().getTurn()).values());
        int  size = moves.size();
        Move move = null;
        if (getGameBoard().getGamePhase() == GamePhase.ONE)
          move = randLinkLinkMove(moves, size);
        else
          move = moveMinMax();
        return move;
    }

    /**
     * This method choses a random move to be made for the AI
     * @param  moves List of available moves
     * @param  size                  size of the List
     * @return                      random move to be made
     */
    private Move randLinkLinkMove(LinkedList<Move>moves, int size) {
        Move   move = null;
        Random rand = new Random();
        int randInt = rand.nextInt(size);
        move = moves.get(randInt);
        while ((size != 2 && move.getType() == MoveType.Surrender)) {
            randInt = rand.nextInt(size);
            move    = moves.get(randInt);
        }
        return move;
    }

    /**
     * This method constructs the connected component of the given agent and stores it in
     * the given list. The paradigm of tge Depth First Search is used to do this.
     * @param agent                  whose connected component is to be cunstructed
     * @param conComp list where connected component is to be stored
     */
    private void connectedContextComponent(Node agent, LinkedList<Node>conComp) {
        if (!conComp.contains(agent)) {
            conComp.add(agent);
            agent.paintBlack();
            LinkedList<Node> neighbors = agent.getNeighbors(SiteColor.WHITE);
            if (neighbors.size() > 0) {
                for (Node node : neighbors) {
                    connectedContextComponent(node, conComp);
                }
            }
        }
    }

    /**
     * This method constructs the connected component of the specified agent
     * with {@link #connectedContextComponent(Node, LinkedList) connectedContextComponent(agent, conComp)} and
     * makes the {@link nowhere2gopp.gamelogic.GameBoard#sites sites} white again with {@link nowhere2gopp.gamelogic.GameBoard#makeSitesWhiteAgain()
     * makeSitesWhiteAgain}.
     * @param agent                  whose connected component is to be cunstructed
     * @param conComp list where connected component is to be stored
     * @param copy                    deep copy of gameboard on which the moves for the AI are calculated
     */
    private void calculateConnectedComponent(Node agent, LinkedList<Node>conComp, GameBoard copy) {
        connectedContextComponent(agent, conComp);
        copy.makeSitesWhiteAgain();
    }

    /**
     * This method returns the weight of the given connected component
     * @param  conComp connected component to be weighted
     * @param  copy    deep copy of gameboard on which the moves for the AI are calculated
     * @return         weight of the given connected component
     */
    private int contextComponentWeight(LinkedList<Node>conComp, GameBoard copy) {
        // erst alle Knoten zählen
        int weight = conComp.size();
        // dann jeden link dazu zählen
        for (Node node : conComp) {
            weight += node.getNeighbors(SiteColor.WHITE).size();
            node.paintBlack();
        }
        copy.makeSitesWhiteAgain();
        return weight;
    }

    /**
     * This method looks if the simulated move could lead to a lose in the next turn
     * @param simulation deep copy of the Gameboard
     * @return true if the simulated move would lead to a lose
     */
    private boolean isLosingMove(GameBoard simulation) {
        boolean   result       = false;
        SiteColor enemySiteCol = getEnemyColor() == PlayerColor.Red ? SiteColor.RED : SiteColor.BLUE;

        LinkedList<Node> neighbors = new LinkedList<Node>();

        // the unoccupied neighbours are added to the list
        for (Node addMe : simulation.getNode(simulation.getAgent(getPlayerColor())).getNeighbors(SiteColor.WHITE)) {
            neighbors.add(addMe);
        }

        // the potential occupied neighbour is added to the list
        for (Node addMe : simulation.getNode(simulation.getAgent(getPlayerColor())).getNeighbors(enemySiteCol)) {
            neighbors.add(addMe);
        }
        int nsize = neighbors.size();

        // if the size of the list is < 2 than the simulated move would lead to a lose next turn
        if (nsize < 2) return true;

        // if the list size is equals 2 than if the enemy can jump on one of the neighbours in the list
        // than the simulated move would lead to a lose in the nest turn
        else if (nsize == 2) {
            try {
                for (int i = 0; i < 2; i++) result = result || simulation.pathFinder(simulation.getNode(simulation.getAgent(getEnemyColor())), neighbors.get(i));
            } catch (NullPointerException x) {
                return true;

                // getAgent was Null, no problem we are probably in gamephase 2
            }
        }
        return result;
    }

    /**
     * GameSituation with the maximum rating
     */
    private GameSituation outterMax;

    /**
     * GameSituation with the minimum rating
     */
    private GameSituation innerMin;

    /**
     * Status that indicates that the AI has won
     */
    private Status playerWin;

    /**
     * Status that indicates that the opponent has won
     */
    private Status enemyWin;

    /**
     * checks if the given {@link nowhere2gopp.gamelogic.player.GameSituation GameSituation} has a better rating compared to {@link #outterMax
     * outterMax}.
     * @param newSit given GameSituation that is compared to the {@link #outterMax outterMax}.
     */
    private void checkMax(GameSituation newSit) {
        if (newSit.getSituation() >= outterMax.getSituation())
         outterMax = new GameSituation(newSit.getSituation(), newSit.getMove());
    }

    /**
     * checks if the given {@link nowhere2gopp.gamelogic.player.GameSituation GameSituation} has a worse rating compared to {@link #innerMin
     * innerMin}.
     * @param newSit given GameSituation that is compared to the {@link #innerMin innerMin}.
     */
    private void checkMin(GameSituation newSit) {
        if (newSit.getSituation() <= innerMin.getSituation())
          innerMin = new GameSituation(newSit.getSituation(), newSit.getMove());
    }

    /**
     * This method compress the possMoves from GamePhase Two of the given color to only contain moves
     * @param col   color of Player
     * @param board deep copy of the Gameboard on which the moves are simulated
     * @return reduced list of phase two moves
     */
    private Collection<Move>compressedPhaseTwoMoves(PlayerColor col, GameBoard board) {
        LinkedList<Move>    result = new LinkedList<Move>();
        LinkedList<SiteSet> links  = new LinkedList<SiteSet>(board.getLinks());
        // for all unoccupied sites construct a SiteTuple with the first unoccupied neighbor
        // For all available links create move
        // reduces the the moves of phase two
        for (Node site : board.getWhiteSites()) {
            LinkedList<Node> neighbors = site.getNeighbors(SiteColor.WHITE);
            if (neighbors.size() != 0) {
                for (SiteSet link : links) {
                    result.add(new Move(new SiteTuple(neighbors.get(0).getSite(), site.getSite()), link));
                }
            }
        }
        return result;
    }

    /**
     * this methode returns a list of moves the specified player
     * @param color player whose moves are to be returned
     * @param copy  simulated Gameboard
     * @return list of moves
     */
    private Collection<Move>getMoves(PlayerColor color, GameBoard copy) {
        Collection<Move> moves = new LinkedList<>();
        if (copy.getGamePhase() == GamePhase.TWO) {
            moves = compressedPhaseTwoMoves(color, copy);
        } else {
            moves = copy.getPossibleMoves(color).values();
        }
        return moves;
    }

    /**
     * This method returns the next move the AI wants to make. To pinpoint which move the AI wants to do
     * by utilizing {@link #outterLoop(GameBoard) outterLoop} and {@link #innerLoop(Move, GameBoard) innerLoop}.
     * @return move to be made
     */
    private Move moveMinMax() {
        outterMax = new GameSituation(Integer.MIN_VALUE, null);
        Serialize copycat = new Serialize();
        GameBoard copy    = (GameBoard)copycat.deepCopyObject(getGameBoard());
        return outterLoop(copy);
    }

    /**
     * This method is given a deep copy of the GameBoard to simulate moves to determine which
     * move would be good given the current Situation. It tests all the moves that the AI can
     * make and utilizes {@link #innerLoop(Move, GameBoard) innerLoop} to test the possible enemy
     * reations of a simulated move. A move is returned early if the tested move leads to a win
     * for the AI. The innerLoop is skipped if the simulated move leads to a lose for the AI.
     * The method {@link nowhere2gopp.gamelogic.GameBoard#rollBackMove(Move, HashMap) rollBackMove} is used to reverte
     * a made move.
     * @param  copy deep copy of the gameboard on which to simulate the moves
     * @return      move the AI wants to make
     */
    private Move outterLoop(GameBoard copy) {
        HashMap<Move, Move> origMoves = copy.copyMoveMap(copy.getPossibleMoves(getPlayerColor()));
        Collection<Move>    selfMoves = getMoves(getPlayerColor(), copy);
        for (Move selfMove : selfMoves) {
            innerMin = new GameSituation(Integer.MAX_VALUE, null);
            if (selfMove.getType() != MoveType.Surrender) {
                copy.make(selfMove);
                Status status = copy.getStatus();
                if (status == playerWin) {
                    return selfMove;
                }
                else if (status == Status.Ok) {
                    if (isLosingMove(copy)) {
                        checkMax(new GameSituation(Integer.MIN_VALUE, selfMove));
                        copy.rollBackMove(selfMove, origMoves);
                        continue;
                    }
                    innerLoop(selfMove, copy);
                    checkMax(innerMin);
                } else {
                    checkMax(new GameSituation(Integer.MIN_VALUE, selfMove));
                }
                copy.rollBackMove(selfMove, origMoves);
            }
        }
        return outterMax.getMove();
    }

    /**
     * This method is called from {@link #outterLoop(GameBoard) outterLoop} to simulate enemy reaction moves
     * of the passed selfMove. If the enemy move leads to the enemy winning, than this selfMove is given a bad rating packed
     * into a {@link nowhere2gopp.gamelogic.player.GameSituation GameSituation} and passed to {@link #checkMin(GameSituation) checkMin(GameSituation)}
     ******and
     * returned to outterLoop.
     * Otherwise the {@link #gameSituation(GameBoard) gameSituation} is used to get a rating for the selfMove,
     * packed into a {@link nowhere2gopp.gamelogic.player.GameSituation GameSituation}  and given
     * to {@link #checkMin(GameSituation) checkMin(GameSituation)}.
     * {@link nowhere2gopp.gamelogic.GameBoard#rollBackMove(Move, HashMap) rollBackMove} is used to revert the simulated enemy moves.
     * @param selfMove move that the AI made
     * @param copy     deep copy of gameboard on which the moves are simulated
     */
    private void innerLoop(Move selfMove, GameBoard copy) {
        Collection<Move>    opponentMoves   = getMoves(getEnemyColor(), copy);
        HashMap<Move, Move> opponentMoveMap = copy.getPossibleMoves(getEnemyColor());
        for (Move opponentMove : opponentMoves) {
            if (opponentMove.getType() != MoveType.Surrender) {
                copy.make(opponentMove);
                Status innerStatus = copy.getStatus();
                if (innerStatus == enemyWin) {
                    checkMin(new GameSituation(Integer.MIN_VALUE, selfMove));
                    copy.rollBackMove(opponentMove, opponentMoveMap);
                    return;
                }
                int situation    = gameSituation(copy);
                GameSituation gS = new GameSituation(situation, selfMove);
                    checkMin(gS);
                copy.rollBackMove(opponentMove, opponentMoveMap);
            }
        }
    }

    /**
     * This method constructs the connected component of both the AI and the enemy with
     * {@link #calculateConnectedComponent(Node, LinkedList, GameBoard) calculateConnectedComponent}.
     * These are weighted with {@link #contextComponentWeight(LinkedList, GameBoard) contextComponentWeight()}
     * The weight of the enemy is than substracted from the weight of the AI and in doing so
     * create a rating for the Situation that the simulated selfMove.
     * @param  copy deep copy of the gameboard on which the moves are simulated
     * @return      rating for simulated selfMove
     */
    private int gameSituation(GameBoard copy) {
        Node self     = copy.getSites().get(copy.getAgent(getPlayerColor()));
        Node opponent = copy.getSites().get(copy.getAgent(getEnemyColor()));
        calculateConnectedComponent(self,     selfContextComponent,     copy);
        calculateConnectedComponent(opponent, opponentContextComponent, copy);
        selfSituation     = contextComponentWeight(selfContextComponent, copy);
        opponentSituation = contextComponentWeight(opponentContextComponent, copy);
        selfContextComponent.clear();
        opponentContextComponent.clear();
        return selfSituation - opponentSituation;
    }
}
