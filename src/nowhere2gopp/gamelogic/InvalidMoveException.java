package nowhere2gopp.gamelogic;
/**
 * Exception when a invalid move has been tried to be made
 */
public class InvalidMoveException extends IllegalStateException {
    public InvalidMoveException()
    {
        super();
    }

    public InvalidMoveException(String msg)
    {
        super(msg);
    }
}
