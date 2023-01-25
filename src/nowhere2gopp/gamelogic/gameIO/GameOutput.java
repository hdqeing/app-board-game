package nowhere2gopp.gamelogic.gameIO;

/**
 * This class specifies an interface for a GUI that can represent our game.
 * @author Marvin Sommer
 */

import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Requestable;
import nowhere2gopp.preset.Status;

public interface GameOutput extends Requestable {
    /**
     * Sets the viewer to Output Information from
     * @param viewer viewer to output information from
     */
    void setViewer(final BoardViewer viewer);

    /**
     * Updates the output with the given move
     * @param move   move to make
     * @param player playercolor of current player
     */
    void update(final Move move, final PlayerColor player);

    /**
     * displays the status we pass
     * @param status status to display
     */
    void showStatus(final Status status);

    /**
     * resets the GUI to an empty version of itself
     */
    void reset();

    /**
     * gets the current gamestate and displays it (useful after reset or loading a game)
     */
    void reload();

    /**
     * closes the output
     */
    void close();
}
