package nowhere2gopp.gamelogic.gameIO;

/**
 * Implementation of assignment 3 b)
 * "Erstellen Sie eine Klasse, die die Schnittstelle nowhere2gopp.preset.Viewer
 * implementiert."
 * @author Marvin Sommer
 * @version 0.2
 */
import java.util.Scanner;

import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.Requestable;

/**
 * This class implements the Text input
 */
public class MoveTextInput implements Requestable {
    public MoveTextInput() {}

    /**
     * Implements the request method of requestable interface, if input cant be parsed we throw an exception
     * @return typed move
     */
    @Override
    public Move request() throws Exception {
        String  s      = "";
        Scanner scanIn = new Scanner(System.in);

        s = scanIn.nextLine(); /*
                                * we dont close the input stream since we might need it later
                                */
        Move result = null;
        result = Move.parse(s);
        return result;
    }
}
