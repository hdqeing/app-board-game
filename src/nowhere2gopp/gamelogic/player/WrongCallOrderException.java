package nowhere2gopp.gamelogic.player;

/**
 * This Exception is to be thrown, when the Status of our main and a playerboard dont match.
 */

public class WrongCallOrderException extends Exception
{
   private static final long serialVersionUID = 1L;

   /**
    * Default Constructor
    */
   public WrongCallOrderException()
   {
     super();
   }

   /**
    * Constructor that lets you pass a message
    * @param msg  Message to pass
    */
   public WrongCallOrderException(String msg)
   {
      super(msg);
   }
}
