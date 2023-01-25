package nowhere2gopp.gamelogic.player;

/**
 * This class implements the abstract player class the other players inherit from.
 * @author Marvin Sommer
 */

import java.rmi.RemoteException;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.gamelogic.gameIO.GameIO;
import nowhere2gopp.gamelogic.gameIO.BoardViewer;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.MoveType;
import nowhere2gopp.preset.Player;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Status;

public abstract class AbstractPlayer implements Player {
    /**
     * Color of this Player
     */
    private PlayerColor playerColor;

    /**
     * Color of enemy Player
     */
    private PlayerColor enemyColor;

    /**
     * GameBoard instance of this player
     */
    private GameBoard board;

    /**
     * Viewer representing the {@link #board} of this player
     */
    protected BoardViewer viewer;

    /**
     * Contains the next expected call type of the game turn cycle
     */
    private NextPlayerMethod next;

    /**
     * Contains our GUI representation of our game
     */
    protected GameIO gui;

    /**
     * Default constructor
     */
    public AbstractPlayer() {}

    /**
     * Abstract method that delivers a valid move corresponding to the player gameboard
     */
    protected abstract Move requestMove() throws Exception, RemoteException;

    /**
     * Returns a {@link nowhere2gopp.preset.Move} thats constructed using the corresponding Playertype that extends this class
     * @return Returns a {@link nowhere2gopp.preset.Move}
     */
    @Override
    public Move request() throws Exception, RemoteException {
        if (next != NextPlayerMethod.Request) {
            throw new WrongCallOrderException("Not your turn");
        }
        Move move = requestMove(); // passes to request method of player
        next = NextPlayerMethod.Confirm;
        board.make(move);
        gui.update(move, playerColor);

        if (move.getType() != MoveType.Surrender) // Move doesnt have a toString for Surrender type moves and would throw a nullpointer exception
                                                  // otherwise
            System.out.println("Your turn: " + move);
        else System.out.println("You gave up");
        return move;
    }

    /**
     * Confirms that the passed {@link nowhere2gopp.preset.Status}  corresponds with the current {@link nowhere2gopp.preset.Status}  of our {@link
     **#board}
     * @param  status         {@link nowhere2gopp.preset.Status} to compare
     */
    @Override
    public void confirm(Status status) throws Exception, RemoteException {
        if (next != NextPlayerMethod.Confirm) {
            throw new WrongCallOrderException("confirm was called in the wrong order");
        }

        if (status != board.getStatus()) {
            throw new StatusMismatchException("Status of Playerboard and Mainboard don't match");
        } else {
            next = NextPlayerMethod.Update;
        }
    }

    /**
     * Updates the {@link #board} with and enemy {@link nowhere2gopp.preset.Move} and verifies if the Status of the
     * Main and Playerboard match
     * @param  opponentMove    {@link nowhere2gopp.preset.Move} to make
     * @param  status          Status to verify
     */
    @Override
    public void update(Move opponentMove, Status status) throws Exception, RemoteException {
        if (next != NextPlayerMethod.Update) {
            throw new WrongCallOrderException("update was called in the wrong order");
        }
        board.make(opponentMove);
        gui.update(opponentMove, enemyColor);

        if (status != board.getStatus()) {
            throw new StatusMismatchException("Status of Playerboard and Mainboard don't match");
        } else {
            next = NextPlayerMethod.Request;
        }
    }

    /**
     * Initialize our Player with a fresh {@link #board} and the corresponding {@link nowhere2gopp.preset.PlayerColor}
     * @param  size            Size of the {@link nowhere2gopp.gamelogic.GameBoard}
     * @param  color           {@link nowhere2gopp.preset.PlayerColor} of this Player
     */
    public void init(int size, PlayerColor color) throws Exception, RemoteException {
        playerColor = color;
        enemyColor  = PlayerColor.Red == playerColor ? PlayerColor.Blue : PlayerColor.Red; // fuer update nuetzlich
        this.board  = new GameBoard(size);
        viewer      = (BoardViewer)board.viewer();

        if (color == PlayerColor.Red) {
            next = NextPlayerMethod.Request;
        } else {
            next = NextPlayerMethod.Update;
        }
    }

    /**
     * Sets the GUI to represent this Players game
     * @param gui GUI to represent the game
     */
    public void setGUI(GameIO gui) {
        this.gui = gui;
    }

    /**
     * Returns our {@link #board}
     * @return {@link #board}
     */
    protected GameBoard getGameBoard() {
        return board;
    }

    /**
     * Returns our {@link #playerColor}
     * @return {@link #playerColor}
     */
    protected PlayerColor getPlayerColor() {
        return playerColor;
    }

    /**
     * Returns our {@link #enemyColor}
     * @return {@link #enemyColor}
     */
    protected PlayerColor getEnemyColor() {
        return enemyColor;
    }
}
