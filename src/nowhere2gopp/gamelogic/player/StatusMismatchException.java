package nowhere2gopp.gamelogic.player;

/**
 * This Exception is to be thrown, when the Status of our main and a playerboard dont match.
 */

public class StatusMismatchException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public StatusMismatchException()
    {
        super();
    }

    /**
     * Constructor that lets you pass a message
     * @param msg  Message to pass
     */
    public StatusMismatchException(String msg)
    {
        super(msg);
    }
}
