package nowhere2gopp.gamelogic.mainprogram;

import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.rmi.*;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.gamelogic.gameIO.*;
import nowhere2gopp.gamelogic.gameIO.BoardViewer;
import nowhere2gopp.gamelogic.player.HumanPlayer;
import nowhere2gopp.gamelogic.player.NetworkPlayer;
import nowhere2gopp.gamelogic.player.RandomPlayer;
import nowhere2gopp.gamelogic.player.Referee;
import nowhere2gopp.gamelogic.player.SimplePlayer;
import nowhere2gopp.preset.ArgumentParser;
import nowhere2gopp.preset.ArgumentParserException;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.Player;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.PlayerType;
import nowhere2gopp.preset.Status;

/**
*This class includes main method for starting the game.
*/
public class Nowhere2gopp {
  /**
  *The type of read player.
  */
  private PlayerType redType;
  /**
  *The input method of red player.
  */
  private boolean redTextInputEnabled;
  /**
  *Red player.
  */
  private Player redPlayer;
  /**
  *The game board of red player.
  */
  private GameBoard redGbd;

  /**
  *The type of blue player.
  */
  private PlayerType blueType;
  /**
  *The input method of blue player.
  */
  private boolean blueTextInputEnabled;
  /**
  *Blue player.
  */
  private Player bluePlayer;
  /**
  The game board of blue player.
  */
  private GameBoard blueGbd;

  /**
  *The size of game board.
  */
  private int gbdSize;
  /**
  *The main game board.
  */
  private GameBoard mainGbd;
  /**
  *Main viewer, used to create main GUI.
  */
  private BoardViewer mainViewer;
  /**
  *Main GUI.
  */
  private GameIO mainGUI;
  /**
  *Referee of the game.
  */
  private Referee gameReferee;
  /**
  *GUI delay for AI player.
  */
  private int delay;
  /**
  *Flag to start a online game.
  */
  private boolean host;
  /**
  *Scanner for text input.
  */
  private Scanner scanner;
  /**
  *Flag to activate online mode.
  */
  private boolean online;
  /**
  *The input method of created player.
  */
  private boolean myTextInputEnabled;

  public Nowhere2gopp() {
    gbdSize = 2;
    delay = 1;
    scanner = new Scanner(System.in);
  }

  /**
  *Make a copy in rmi.
  *@param p
  *       Local player.
  *
  *@param host
  *       My hostname
  *
  *@param port
  *       Port used by rmi.
  *
  *@param name
  *       Playername of choice.
  */
  private void offer(final Player p, final String host, final int port, final String name) {
    try {
      Registry registry = null;
      registry = LocateRegistry.getRegistry(host, port);
      registry.rebind(name, p);
      System.out.println("Player prepared!");
      System.out.println("Hostname:");
      System.out.println(host);
      System.out.println("Port:");
      System.out.println(port);
      System.out.println("Player Name:");
      System.out.println(name);
    } catch (RemoteException e) {
      System.err.println("Player cannot be prepared! Please make sure rmi service is enabled!");
      System.exit(1);
    } catch (Exception e){
      System.out.println("Player cannot be prepared!");
      System.exit(1);
    }
  }

  /**
  *Create a reference to remote player.
  *@param host
  *       The hostname of expected player.
  *
  *@param port
  *       The port used by expected player.
  *
  *@param name
  *       The name of expected player.
  *
  *@return a reference to remote player
  *
  */
  private Player find(final String host, final int port, final String name) {
    Player p = null;
    try{
      Registry registry = LocateRegistry.getRegistry(host, port);
      p = (Player) registry.lookup(name);
      System.out.println("Player " + name + " found!");
    } catch (Exception e) {
      System.err.println("Player " + name + " not found!");
      System.exit(1);
    }
    return p;
  }

  /**
  *This methods creates a local player.
  *@param playerType
  *       The type of the player to be created.
  *
  *@param playerColor
  *       The color of player to be created.
  *
  *@param size
  *       The size of game board.
  *
  *@param gui
  *       GUI for player to be created.
  *
  *@param textEnabled
  *       If true, text input will be activated (only for human player).
  *
  *@param textInput
  *       The MoveTextInput object which reads in moves from text.
  *
  *@return created player.
  */
  private Player createLocalPlayer(final PlayerType playerType, final PlayerColor playerColor, final int size, final GameIO gui, final boolean textEnabled, final MoveTextInput textInput){
    Player newPlayer = null;

    //Create and initialize player.
    switch (playerType){
      case Human:
      if (textEnabled){
        newPlayer = new HumanPlayer(textInput, gui);
      }
      else{
        newPlayer = new HumanPlayer(gui);
      }
      break;
      case SimpleAI:
      newPlayer = new SimplePlayer(gui);
      break;
      case RandomAI:
      newPlayer = new RandomPlayer(gui);
      break;
      case Remote:
      System.out.println("Please activate online mode to create a remote player!");
      System.exit(1);
      break;
    }
    try{
      newPlayer.init(size, playerColor);
    } catch (Exception e){
      System.err.println("Player cannot be created!");
      System.err.println(e.getMessage());
      System.exit(1);
    }
    return newPlayer;
  }

  /**
  *This method converts a string to player type.
  *@param myType
  *       The type of player in string.
  *
  *@return corresponding player type
  */
  protected PlayerType toPlayerType(final String myType){
    PlayerType myPlayerType = null;

    if (myType.equalsIgnoreCase("Human")){
      myPlayerType = PlayerType.Human;
    }
    else if (myType.equalsIgnoreCase("RandomAI")){
      myPlayerType = PlayerType.RandomAI;
    }
    else {
      myPlayerType = PlayerType.SimpleAI;
    }

    return myPlayerType;
  }

  /**
  *This method converts a string to player color.
  *@param myColor
  *       The color of player in string.
  *
  *@return corresponding player color
  */
  protected PlayerColor toPlayerColor(final String myColor){
    PlayerColor myPlayerColor = null;

    if (myColor.equalsIgnoreCase("Red")){
      myPlayerColor = PlayerColor.Red;
    }
    else if (myColor.equalsIgnoreCase("Blue")){
      myPlayerColor = PlayerColor.Blue;
    }
    else{
      System.out.println("Invalid Player Color!");
      System.exit(1);
    }

    return myPlayerColor;
  }

  /**
  *This method makes necessary preparation to start a game.
  *The commandline arguments are parsed by {@link nowhere2gopp.preset.ArgumentParser ArgumentParser} to determine the mode of current game.
  *For an offline game (online set to false), one has to specify the type of both players and the size of the game board with commandline arguments.
  *For human player, one has to determine if move is read in with text or graphical user interface(GUI).
  *For an online game (online set to true), one can either create a player or start a game (given two player created).
  *In order to create a player, one has to specify the color of the player, the type of the player, and the size of game board.
  *Besides, one has to provide a hostname (by default, IP address is used), a port (by default 1099), and a player name so that created player can be found by a game host.
  *All the parameters will be asked interactively and read in with standard input.
  *After two players have been created, a game can be started. In this case, the program will ask for the hostnames, the ports, and the player names of two players respectively.
  */
  private void init(final String[] s){
    try {
      // parse command line arguments
      ArgumentParser parser = new ArgumentParser(s);
      blueType = parser.getBlue();
      blueTextInputEnabled = parser.isBlueTextEnabled();
      redType = parser.getRed();
      redTextInputEnabled = parser.isRedTextEnabled();
      gbdSize = parser.getSize();
      delay = parser.getDelay();
      online = parser.isOnline();
      host = parser.isHost();

      if (!online){
        //all necessary parameters are given with command line arguments
        startLocalGame(redType, redTextInputEnabled, blueType, blueTextInputEnabled, gbdSize);
      } else {
        if (!host){
          //pick player color
          System.out.println("Please choose your color(Red/Blue): ");
          String myColor = scanner.nextLine();
          while ((!myColor.equalsIgnoreCase("Red")) && (!myColor.equalsIgnoreCase("Blue"))){
            System.out.println("Invalid Player Color! Allowed player colors include \"Red\" and \"Blue\":");
            myColor = scanner.nextLine();
          }

          //select player type
          System.out.println("What kind of player you want to create (Human/RandomAI/SimpleAI)?");
          String myType = scanner.nextLine();
          while ((!myType.equalsIgnoreCase("Human")) && (!myType.equalsIgnoreCase("RandomAI")) && (!myType.equalsIgnoreCase("SimpleAI"))){
            System.out.println("Invalid Player Type! Allowed player types include \"Human\" , \"RandomAI\" and \"SimpleAI\":");
            myType = scanner.nextLine();
          }

          //read in move by text
          System.out.println("Make your move by text input(y/n)?");
          String myTextEnabled = scanner.nextLine();
          while ((!myTextEnabled.equalsIgnoreCase("y")) && (!myTextEnabled.equalsIgnoreCase("n"))){
            System.out.println("Invalid Input! For enabling text input press \"y\", for graphical input press \"n\": ");
            myTextEnabled = scanner.nextLine();
          }
          boolean myTextInputEnabled;
          if (myTextEnabled.equalsIgnoreCase("y")){
            myTextInputEnabled = true;
          }
          else {
            myTextInputEnabled = false;
          }

          //give the hostname
          System.out.println("Please enter your hostname (Press Enter key to use your IP Address as your hostname): ");
          String myHostName = scanner.nextLine();

          //use ip as default hostname
          if (myHostName.length() == 0){
            try{
              InetAddress myLocalHost = InetAddress.getLocalHost();
              myHostName = myLocalHost.getHostAddress();
              System.out.println("Your hostname is " + myHostName);
            } catch (Exception e){
              System.out.println("Cannot get hostname!");
            }
          }

          //give a port
          System.out.println("Please enter port (Press Enter to use 1099): ");
          String myPortString = scanner.nextLine();
          int myPort;
          if (myPortString.length() == 0){
            myPort = 1099;
          }
          else{
            myPort = Integer.parseInt(myPortString);
          }

          //give a player name
          System.out.println("Please enter a name for your player: ");
          String myPlayerName = scanner.nextLine();

          createPlayer(myColor, myType, myTextInputEnabled, gbdSize, myHostName, myPort, myPlayerName);
        }
        else {
          //find red player
          System.out.println("Please enter the hostname of red player: ");
          String redHostName = scanner.nextLine();
          System.out.println("Please enter the port of red player: ");
          int redPort = Integer.parseInt(scanner.nextLine());
          System.out.println("Please enter the player name of red player: ");
          String redPlayerName = scanner.nextLine();

          //find blue player
          System.out.println("Please enter the hostname of blue player: ");
          String blueHostName = scanner.nextLine();
          System.out.println("Please enter the port of blue player: ");
          int bluePort = Integer.parseInt(scanner.nextLine());
          System.out.println("Please enter the player name of blue player: ");
          String bluePlayerName = scanner.nextLine();

          //start the game
          startOnlineGame(redHostName, redPort, redPlayerName, blueHostName, bluePort, bluePlayerName);
        }
      }
    } catch (ArgumentParserException e) {
      System.err.println(e.getMessage());
      System.exit(0);
    }
  }

  /**
  *Calling this method will start an offline game.
  *
  *@param redPlayerType
  *       The player type of red player. See {@link nowhere2gopp.preset.PlayerType PlayerType}
  *
  *@param redTextEnabled
  *       The input method of red player. If turned on, moves of red player will be read in with standard input, otherwise it will be read in with graphical user interface(GUI).
  *
  *@param bluePlayerType
  *       The player type of blue player. See {@link nowhere2gopp.preset.PlayerType PlayerType}
  *
  *@param blueTextEnabled
  *       The input method of blue player. If turned on, moves of blue player will be read in with standard input, otherwise it will be read in with graphical user interface(GUI).
  *
  *@param gbdSize
  *       The size of game board. It takes integer from 1 (inclusive) to 5(inclusive).
  */
  protected void startLocalGame(final PlayerType redPlayerType, final boolean redTextEnabled, final PlayerType bluePlayerType, final boolean blueTextEnabled, final int gbdSize){
    //main game board and main GUI
    mainGbd = new GameBoard(gbdSize);
    MoveTextInput textInput = null;
    if (redTextEnabled || blueTextEnabled){
      textInput = new MoveTextInput();
    }

    mainViewer = new BoardViewer(mainGbd);
    mainGUI = new GameIO(mainViewer);
    //create red player
    redPlayer = createLocalPlayer(redPlayerType, PlayerColor.Red, gbdSize, mainGUI, redTextEnabled, textInput);

    //create blue player
    bluePlayer = createLocalPlayer(bluePlayerType, PlayerColor.Blue, gbdSize, mainGUI, blueTextEnabled, textInput);

    run();
  }

  /**
  *Calling this method will start an online game. See {@link java.rmi.registry.LocateRegistry LocateRegistry}.
  *@param redHostname
  *       The hostname of red player.
  *
  *@param redPort
  *       The port of red player.
  *
  *@param redPlayerName
  *       The player name of red player. See {@link java.rmi.registry.Registry Registry}
  *
  *@param blueHostname
  *       The hostname of blue player.
  *
  *@param bluePort
  *       The port of blue player.
  *
  *@param bluePlayerName
  *       The player name of blue player
  */
  protected void startOnlineGame(final String redHostname, final int redPort, final String redPlayerName, final String blueHostname, final int bluePort, final String bluePlayerName){
    //set online mode
    online = true;

    //access both players
    redPlayer = find(redHostname, redPort, redPlayerName);
    bluePlayer = find(blueHostname, bluePort, bluePlayerName);

    //start game
    run();
  }

  /**
  *Create a player and make the player online accessible.
  *@param myColor
  *       The color of the player.
  *
  *@param myType
  *       The type of the player.
  *
  *@param myTextInputEnabled
  *       The input method of created player. If turned on, moves of created player will be read in with standard input, otherwise it will be read in with graphical user interface(GUI).
  *
  *@param gbdSize
  *       The size of game board.
  *
  *@param myHostname
  *       The hostname of created player.
  *
  *@param myPort
  *       The port of created player.
  *
  *@param myPlayerName
  *       The name of created player.
  */
  protected void createPlayer(final String myColor, final String myType, final boolean myTextInputEnabled, final int gbdSize, final String myHostname, final int myPort, final String myPlayerName){
    //prepare player GUI
    GameBoard myGbd = new GameBoard(gbdSize);
    BoardViewer myViewer = new BoardViewer(myGbd);
    GameIO myGUI = new GameIO(myViewer);

    MoveTextInput myTextInput = null;
    if (myTextInputEnabled){
      myTextInput = new MoveTextInput();
    }

    PlayerColor myPlayerColor = toPlayerColor(myColor);
    PlayerType myPlayerType = toPlayerType(myType);

    //create and initialize player
    Player myLocalPlayer = null;
    myLocalPlayer = createLocalPlayer(myPlayerType, myPlayerColor, gbdSize, myGUI, myTextInputEnabled, myTextInput);

    //create network player
    Player myPlayer = null;
    try{
      myPlayer = new NetworkPlayer(myLocalPlayer);
      myPlayer.init(gbdSize, myPlayerColor);
    } catch (Exception e){
      System.err.println(e.getMessage());
      System.err.println("Player cannot be created!");
    }

    //make the player online accessible
    offer(myPlayer, myHostname, myPort, myPlayerName);
  }

  /**
  *Prepare the game referee and ask moves from both players in turn.
  */
  private void run(){
    //at online mode, main program has to prepare main game board and GUI
    if (online){
      mainGbd = new GameBoard(gbdSize);
      mainViewer = new BoardViewer(mainGbd);
      mainGUI = new GameIO(mainViewer);
    }

    //initialize referee
    gameReferee = new Referee(mainGbd, redPlayer, bluePlayer);

    Status currentStatus = mainGbd.getStatus();
    PlayerColor currentPlayer = mainGbd.getTurn();
    boolean isRedTurn = (currentPlayer == PlayerColor.Red);
    PlayerType currentPlayerType = ((isRedTurn) ? redType : blueType);

    Move nextMove = null;
    while (currentStatus == Status.Ok){
      //ask for moves from the players
      try{
        nextMove = gameReferee.request();
      } catch (Exception e){
        e.printStackTrace();
        System.err.println("Could not request move from " + currentPlayer + " player!");
      }

      //Make the move on main game board.
      mainGbd.make(nextMove);
      mainGUI.update(nextMove, currentPlayer);

      //Update player's game board and GUI for an online game.

      currentStatus = mainGbd.getStatus();

      try{
        if (isRedTurn){
          redPlayer.confirm(currentStatus);
          bluePlayer.update(nextMove, currentStatus);
        }
        else {
          bluePlayer.confirm(currentStatus);
          redPlayer.update(nextMove, currentStatus);
        }
      } catch (Exception e) {
        System.err.println("Could not update " + ((currentPlayer == PlayerColor.Red) ? "BluePlayer's " : "RedPlayer's ") + "Board!");
      }

      if (redType != PlayerType.Human && blueType != PlayerType.Human){
        try{
          TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e){
          System.err.println(e.getMessage());
        }
      }

      currentPlayer = mainGbd.getTurn();
      isRedTurn = (currentPlayer == PlayerColor.Red);
      currentPlayerType = ((isRedTurn) ? redType : blueType);
    }
  }

  public static void main(final String[] s) {
    Nowhere2gopp newGame = new Nowhere2gopp();
    if (s[0].equals("loading")){
      LoadingPage loadingPage = new LoadingPage(newGame);
      loadingPage.init();
    } else {
      newGame.init(s);
    }
  }

}
