package nowhere2gopp.gamelogic.player;

/**
 * This class implements the random player
 *
 * @author Marvin Sommer
 */

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Random;

import nowhere2gopp.gamelogic.gameIO.GameIO;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.MoveType;


public class RandomPlayer extends AbstractPlayer {
    /*
     * Default constructor
     */
    public RandomPlayer() {
        super();
    }

    /**
     * Constructor that sets the {@link #gui} to represent the gamestate
     * @param gui new {@link #gui}
     */
    public RandomPlayer(GameIO gui) {
        super();
        setGUI(gui);
    }

    /**
     * Returns a random {@link nowhere2gopp.preset.Move}
     * @return random {@link nowhere2gopp.preset.Move}
     */
    protected Move requestMove() throws Exception, RemoteException {
        LinkedList<Move> moves =  new LinkedList<Move>(viewer.getPossibleMoves().values());
        int  size = moves.size();
        Move move = null;

        Random rand = new Random();
        int randInt = rand.nextInt(size);
        move = moves.get(randInt);

        while ((size != 2 && move.getType() == MoveType.End) ||  move.getType() == MoveType.Surrender) {
            randInt = rand.nextInt(size);
            move = moves.get(randInt);
        }
        return move;
    }
}
