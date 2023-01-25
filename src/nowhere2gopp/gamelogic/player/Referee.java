package nowhere2gopp.gamelogic.player;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.Player;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Requestable;

/**
 * This class realizes a referee for the game, whose main function is to request moves from both players in turn.
 */
public class Referee implements Requestable {
    // -----------------Attributes--------------

    /**
       *Main game board.
     */
    private GameBoard gbd;

    /**
       *red player
     */
    private Player playerRed;

    /**
       *blue player
     */
    private Player playerBlue;

    // -----------------Constructors------------
    public Referee(final GameBoard gameBoard, final Player redPlayer,final Player bluePlayer) {
        gbd = gameBoard;
        playerRed = redPlayer;
        playerBlue = bluePlayer;
    }

    // -----------------Methods-----------------

    /**
       *This method requests move from both players.
       *@return next move to be performed on the game board.
     */
    public Move request() throws Exception {
        Move nextMove             = null;
        PlayerColor currentPlayer = gbd.getTurn();

        if (currentPlayer == PlayerColor.Red) {
            // request move and make the move on its own game board
            nextMove = playerRed.request();
        } else {
            // request move and make the move on its own game board
            nextMove = playerBlue.request();
        }
        return nextMove;
    }
}
