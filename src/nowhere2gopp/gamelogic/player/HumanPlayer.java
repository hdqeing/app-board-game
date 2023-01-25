package nowhere2gopp.gamelogic.player;

/**
 * This class implements the Human Player
 *
 * @author Marvin Sommer
 */
import java.rmi.RemoteException;
import java.util.HashMap;

import nowhere2gopp.gamelogic.gameIO.BoardViewer;
import nowhere2gopp.gamelogic.gameIO.GameIO;
import nowhere2gopp.gamelogic.gameIO.MoveTextInput;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.Requestable;

public class HumanPlayer extends AbstractPlayer {
    /**
     * Requestable implementation for human input
     */
    private Requestable request;

    /**
     * text input flag
     */
    private boolean text;

    /**
     * Contains the move thats constructed after a {@link #requestMove} call
     */
    private Move move;

    /**
     * Constructor that takes a {@link nowhere2gopp.preset.Requestable} implementation to pass the {@link #requestMove} call to
     * @param request
     */
    public HumanPlayer(Requestable request) {
        super();
        setRequestable(request);
    }

    /**
     * Constructor that takes two {@link nowhere2gopp.preset.Requestable Requestables}, the first one being the one to forward our
     * {link #requestMove} calls to, the second one should has to be an implementation with graphical output
     * @param request       our Object to request moves from
     * @param gui           our GUI to represent the current gamestate
     */
    public HumanPlayer(Requestable request, Requestable gui) {
        super();
        setRequestable(request);
        setGUI((GameIO)gui);
    }

    /**
     * Passes the request to our corresponding {@link nowhere2gopp.gamelogic.player.AbstractPlayer#request} Object
     */
    protected Move requestMove() throws Exception, RemoteException {
        move = null;
        if (isText()) {
            System.out.println("Please Enter your turn ");
            getMoveFromText();
        } else {
            move = getRequestable().request();
        }
        return move;
    }

    /**
     * Used when the {@link nowhere2gopp.gamelogic.player.AbstractPlayer#request}  Object is a {@link nowhere2gopp.gamelogic.gameIO.MoveTextInput
     *"Text input"}
     * requests text based input until a valid move is passed.
     */
    private void getMoveFromText() {
        HashMap<Move, Move> possMoves = viewer.getPossibleMoves();
        MoveTextInput text = new MoveTextInput();
        while (move == null) {
            try {
                move = request.request();
                if (!(possMoves.containsKey(move))) {
                    move = null;
                    System.out.println("Invalid move, try again...");
                }
            } catch (Exception ex) {
                move = null;
            }
        }
    }

    /**
     * Returns the current value of {@link #text}
     *
     * @return  boolean  {@link #text}
     */
    private boolean isText() {
        return text;
    }

    /**
     * Returns the current {@link #request} Object
     *
     * @return  Requestable  {@link #request} Object
     */
    public Requestable getRequestable() {
        return request;
    }

    /**
     * Sets our current Request Object and determines if it is a textinput, toggles the corresponding flag {@link #text}
     *
     * @param   input  {@link nowhere2gopp.preset.Requestable} implementation to set our input to
     */
    private void setRequestable(Requestable input) {
        request = input;

        if (request instanceof GameIO) {
            text = false;
            gui  = (GameIO)request;
        } else {
            text = true;
            //gui  = new GameIO((BoardViewer)getGameBoard().viewer());
        }
    }
}
