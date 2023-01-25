package nowhere2gopp.gamelogic.player;
import java.util.*;
import nowhere2gopp.gamelogic.*;
import nowhere2gopp.preset.*;

/**
 * Container class for the Moves and the Gamesituationvalue calculated in the SimplePlayer class
 * @author Benedikt W. Berg
 */
public class GameSituation {
    /**
     * int for the rating of given move
     */
    private int rating;

    /**
     * move contender that is to be select be the SimplePlayer
     */
    private Move contenderMove;

    /**
     * Construktor that initializes this container with a rating and its corresponding move
     * @param moveRating Integer value of the rating
     * @param move Move object that corresponds with te rating
     */
    public GameSituation(int  moveRating, Move move) {
        rating = moveRating;
        contenderMove = move;
    }

    public GameSituation(GameSituation copy) {
        rating = copy.getSituation();
        contenderMove = copy.getMove();
    }

    /**
     * Returns the stored Rating of this Container
     * @return Stored rating
     */
    public int getSituation() {
        return rating;
    }

    /**
     * Returns the stored move of this Container
     * @return Stored move
     */
    public Move getMove() {
        return contenderMove;
    }

    /**
     * Returns the stored Rating of this Container
     * @return String of rating integer plus the toString of move
     */
    public String toString() {
        return rating + " " + contenderMove;
    }
}
