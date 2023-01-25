package nowhere2gopp.gamelogic.mainprogram;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.InetAddress;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import nowhere2gopp.preset.PlayerType;

/**
*This class can be used to generate a simple graphical user interface for loading into the game.
*Initially, one can choose to run a game on local mode or on online mode.
*<p> At local mode, the user could specify the type of both players and the size of game board. If one player
* is Human player, the input method must be specified so that the move to be operated can be read in.
*<p> At online mode, the user could either create a player or start an online game.
*<p> If the user want to create a player, he/she simply need to specify the type of his player, input method, the hostname (by default IP address is used as hostname), the port (by default: 1099) and the player name.
*<p> If the user intend to start an online game, he/she will have to find two players of different colors but the same game board size with their individual hostname, port and player name.
*If the user he/she want to participate in the game, he/she will have to first create a player.
*/
public class LoadingPage {
//-------------------Variables------------------
    /**
    *Main program, on which the actions are performed.
    */
    private Nowhere2gopp mainProgram;
    /**
    *List of all player types.
    */
    private String[] playerTypeList;
    /**
    *List of all allowed game board size.
    */
    private Integer[] gbdSizeList;

    /**
    *Frame to show the loading interface.
    */
    private JFrame frame;
    /**
    *Panel of buttons.
    */
    private JPanel menu;
    /**
    *Button to return to main menu.
    */
    private JButton backButton;
    /**
    *The size of the game board.
    */
    private int gbdSize;

    /**
    *The type of red player, read from player type menu.
    */
    private String redPlayerType;
    /**
    *The input method of red player. If true, moves of red player will be read in from text.
    */
    private boolean redTextInputEnabled;
    /**
    *The hostname of red player. Used to find the player by rmi.
    */
    private String redHostname;
    /**
    *The port used by red player. Used to find the player by rmi.
    */
    private int redPort;
    /**
    *The name of red player. Used to find the player by rmi.
    */
    private String redPlayerName;

    /**
    *The type of blue player.
    */
    private String bluePlayerType;
    /**
    *The input method of blue player. If true, moves of blue player will be read in from text.
    */
    private boolean blueTextInputEnabled;
    /**
    *The hostname of blue player. Used to find the player by rmi.
    */
    private String blueHostname;
    /**
    *The port used by blue player. Used to find the player by rmi.
    */
    private int bluePort;
    /**
    *The name of blue player. Used to find the player by rmi.
    */
    private String bluePlayerName;

    /**
    *The color of player to be created.
    */
    private String myPlayerColor;
    /**
    *The type of player to be created.
    */
    private String myPlayerType;
    /**
    *The input method of player to be created.
    */
    private boolean myTextInputEnabled;
    /**
    *The game board size of the player to be created.
    */
    private int myGbdSize;
    /**
    *The hostname of the player to be created.
    */
    private String myHostname;
    /**
    *The port used by the player to be created.
    */
    private int myPort;
    /**
    *The name of the player to be created.
    */
    private String myPlayerName;



  //-------------------Constructor----------------
    protected LoadingPage(Nowhere2gopp mainProgram){
      this.mainProgram = mainProgram;
      //create frame and panel
      frame = new JFrame("Nowhere2go - An App Classic");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      menu = new JPanel();

      redPlayerType = "Human";
      bluePlayerType = "Human";
      gbdSize = 1;

      playerTypeList = new String[3];
      playerTypeList[0] = "Human";
      playerTypeList[1] = "RandomAI";
      playerTypeList[2] = "SimpleAI";

      //game board size menu
      gbdSizeList = new Integer[5];
      for (int i = 0; i < 5; i++){
        gbdSizeList[i] = i + 1;
      }

      //button to return to game mode menu
      backButton = new JButton("BACK");
      backButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          init();
          menu.revalidate();
          menu.repaint();
        };
      });
    }

  //-------------------Methods--------------------
    /**
    *Game mode menu. Select between local game (single player) and online game.
    */
    protected void init(){
      menu.removeAll();
      menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

      //button to start a local game
      JButton buttonLocalGame = new JButton("Local Game");
      buttonLocalGame.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          startLocalGame();
          menu.revalidate();
          menu.repaint();
        };
      });

      //button to start an online game
      JButton buttonOnlineGame = new JButton("Online Game");
      buttonOnlineGame.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          selectOnlineAction();
          menu.revalidate();
          menu.repaint();
        };
      });

      //exit button
      JButton exitButton = new JButton("Exit");
      exitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          System.exit(0);
        };
      });

      //game mode panel
      JPanel gameModePanel = new JPanel(new GridLayout(2, 1));
      gameModePanel.add(buttonLocalGame);
      gameModePanel.add(buttonOnlineGame);

      menu.add(gameModePanel);
      menu.add(exitButton);
      menu.setPreferredSize(new Dimension(600, 400));

      //show game mode menu
      frame.add(menu);
      frame.pack();
      frame.setVisible(true);
    }

    /**
    *Set parameters for a local game.
    */
    private void startLocalGame(){
      menu.removeAll();

      //input method panel for red player
      JPanel redInputMethodPanel = createInputMethodPanel("red");

      //red player type
      JComboBox<String> redPlayerTypeMenu = createPlayerTypeMenu("red", redInputMethodPanel);
      //input method panel for blue player
      JPanel blueInputMethodPanel = createInputMethodPanel("blue");

      //blue player type
      JComboBox<String> bluePlayerTypeMenu = createPlayerTypeMenu("blue", blueInputMethodPanel);

      //game board size
      JComboBox<Integer> gbdSizeMenu = createGbdSizeMenu("local");

      //button to start a local game
      JButton startLocalGameButton = new JButton("GO!");
      startLocalGameButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          frame.setVisible(false);
          PlayerType redType = mainProgram.toPlayerType(redPlayerType);
          PlayerType blueType = mainProgram.toPlayerType(bluePlayerType);

          //new anynomous thread
          Thread gameThread = new Thread(){
            @Override
            public void run(){
              mainProgram.startLocalGame(redType, redTextInputEnabled, blueType, blueTextInputEnabled, gbdSize);
            }
          };
          gameThread.start();
        };
      });

      menu.setLayout(new GridLayout(6, 2));

      //red player type menu
      menu.add(new JLabel("Red Player :", JLabel.RIGHT));
      menu.add(redPlayerTypeMenu);

      //GUI input or text input(red player)
      menu.add(new Label(" "));
      menu.add(redInputMethodPanel);

      //blue player type menu
      menu.add(new JLabel("Blue Player :", JLabel.RIGHT));
      menu.add(bluePlayerTypeMenu);

      //GUI input or text input(blue player)
      menu.add(new Label(" "));
      menu.add(blueInputMethodPanel);

      //game board size menu
      menu.add(new JLabel("Game Board Size :", JLabel.RIGHT));
      menu.add(gbdSizeMenu);

      //start game and back button
      menu.add(startLocalGameButton);
      menu.add(backButton);

      menu.setPreferredSize(new Dimension(600, 400));
    }

    /**
    *Panel including button for starting an online game and button for creating a player.
    */
    private void selectOnlineAction(){
      menu.removeAll();
      menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

      //prepare button to start online game
      JButton startGameButton = new JButton("Start a New Game!");
      startGameButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          startOnlineGame();
          menu.revalidate();
          menu.repaint();
        };
      });

      //prepare button to create a player
      JButton createPlayerButton = new JButton("Create a New Player!");
      createPlayerButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          createPlayer();
          menu.revalidate();
          menu.repaint();
        };
      });

      JPanel onlineGamePanel = new JPanel(new GridLayout(2, 1));
      onlineGamePanel.add(startGameButton);
      onlineGamePanel.add(createPlayerButton);

      menu.add(onlineGamePanel);
      menu.add(backButton);
      menu.setPreferredSize(new Dimension(600, 400));
    }

    /**
    *Read in the information about players to start a game.
    */
    private void startOnlineGame(){
      menu.removeAll();
      menu.setLayout(new GridLayout(5, 3));

      //button to start an online game
      JButton startOnlineGameButton = new JButton("GO!");
      startOnlineGameButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          frame.setVisible(false);

          Thread gameThread = new Thread(){
            @Override
            public void run(){
              mainProgram.startOnlineGame(redHostname, redPort, redPlayerName, blueHostname, bluePort, bluePlayerName);
            }
          };

          gameThread.start();
        };
      });

      //prepare hostname textfield for red player
      JTextField redHostnameTxtfield = new JTextField(20);
      redHostnameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          redHostname = redHostnameTxtfield.getText();
        }
      });

      //prepare hostname textfield for blue player
      JTextField blueHostnameTxtfield = new JTextField(20);
      blueHostnameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          blueHostname = blueHostnameTxtfield.getText();
        }
      });

      //prepare port textfield for red player
      JTextField redPortTxtfield = new JTextField("1099", 20);
      redPortTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          redPort = Integer.parseInt(redPortTxtfield.getText());
        }
      });

      //prepare port textfield for blue player
      JTextField bluePortTxtfield = new JTextField("1099", 20);
      bluePortTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          bluePort = Integer.parseInt(bluePortTxtfield.getText());
        }
      });

      //prepare player name textfield for red player
      JTextField redPlayerNameTxtfield = new JTextField(20);
      redPlayerNameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          redPlayerName = redPlayerNameTxtfield.getText();
        }
      });

      //prepare player name textfield for blue player
      JTextField bluePlayerNameTxtfield = new JTextField(20);
      bluePlayerNameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          bluePlayerName = bluePlayerNameTxtfield.getText();
        }
      });

      //create formular for player information input
      menu.add(new Label(" "));
      menu.add(new JLabel("Red Player: ", JLabel.CENTER));
      menu.add(new JLabel("Blue Player: ", JLabel.CENTER));
      menu.add(new JLabel("Hostname: ", JLabel.RIGHT));
      menu.add(redHostnameTxtfield);
      menu.add(blueHostnameTxtfield);
      menu.add(new JLabel("Port: ", JLabel.RIGHT));
      menu.add(redPortTxtfield);
      menu.add(bluePortTxtfield);
      menu.add(new JLabel("Playername: ", JLabel.RIGHT));
      menu.add(redPlayerNameTxtfield);
      menu.add(bluePlayerNameTxtfield);
      menu.add(new Label(" "));
      menu.add(startOnlineGameButton);
      menu.add(backButton);
    }

    /**
    *Read in the information to create a player.
    */
    private void createPlayer(){
      menu.removeAll();

      //player color menu
      String[] playerColor = {"Red", "Blue"};
      JComboBox<String> playerColorBox = new JComboBox<>(playerColor);
      playerColorBox.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          JComboBox cb = (JComboBox)e.getSource();
          myPlayerColor = (String)cb.getSelectedItem();
        };
      });

      //input method panel
      JPanel myInputMethodPanel = createInputMethodPanel("my");

      //type of my player
      JComboBox<String> myPlayerTypeMenu = createPlayerTypeMenu("my", myInputMethodPanel);

      //my game board size menu
      JComboBox<Integer> myGbdSizeMenu = createGbdSizeMenu("my");

      //get ip address
      String ipAddress = "Cannot get IP-Address!";
      try{
        InetAddress inetAddress = InetAddress.getLocalHost();
        ipAddress = inetAddress.getHostAddress();
      } catch (Exception e){
        System.out.println("Cannot get IP-Address!");
      }

      //hostname textfield
      JTextField myHostNameTxtfield = new JTextField(ipAddress, 20);
      myHostname = ipAddress;
      myHostNameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          myHostname = myHostNameTxtfield.getText();
        }
      });

      //port textfield
      JTextField myPortTxtfield = new JTextField("1099", 20);
      myPort = 1099;
      myPortTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          myPort = Integer.parseInt(myPortTxtfield.getText());
        }
      });

      //player name textfield
      JTextField myPlayerNameTxtfield = new JTextField(20);
      myPlayerNameTxtfield.addFocusListener(new FocusListener(){
        public void focusGained(FocusEvent e){}

        public void focusLost(FocusEvent e){
          myPlayerName = myPlayerNameTxtfield.getText();
        }
      });

      //button to create player
      JButton confirmButton = new JButton("Create Player!");
      confirmButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          frame.setVisible(false);
          Thread gameThread = new Thread(){
            public void run(){
              mainProgram.createPlayer(myPlayerColor, myPlayerType, myTextInputEnabled, myGbdSize, myHostname, myPort, myPlayerName);
            };
          };
          gameThread.start();
        };
      });

      menu.setLayout(new GridLayout(8, 2));

      menu.add(new JLabel("Take a Side: ", JLabel.RIGHT));
      menu.add(playerColorBox);

      menu.add(new JLabel("My Player Type: ", JLabel.RIGHT));
      menu.add(myPlayerTypeMenu);

      menu.add(new JLabel(" "));
      menu.add(myInputMethodPanel);

      menu.add(new JLabel("Game Board Size: ", JLabel.RIGHT));
      menu.add(myGbdSizeMenu);

      menu.add(new JLabel("Hostname: ", JLabel.RIGHT));
      menu.add(myHostNameTxtfield);

      menu.add(new JLabel("Port: ", JLabel.RIGHT));
      menu.add(myPortTxtfield);

      menu.add(new JLabel("Player Name: ", JLabel.RIGHT));
      menu.add(myPlayerNameTxtfield);

      menu.add(confirmButton);
      menu.add(backButton);
    }

    /**
    *This method creates a menu to select the size of game board.
    *@param gameMode
    *       Takes "local" or "online" depending on the game modes.
    */
    private JComboBox<Integer> createGbdSizeMenu(final String gameMode){
      JComboBox<Integer> gbdSizeMenu = new JComboBox<>(gbdSizeList);
      gbdSizeMenu.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          JComboBox cb = (JComboBox)e.getSource();
          if (gameMode == "local"){
            gbdSize = (int)cb.getSelectedItem();
          }
          else {
            myGbdSize = (int)cb.getSelectedItem();
          }
        };
      });
      return gbdSizeMenu;
    }

    /**
    *This method creates a menu to select the type of player.
    *@param pColor
    *       Takes value of "red", "blue" or "my".
    *
    *@param inputMethodPanel
    *       Corresponding input method pane.
    */
    private JComboBox<String> createPlayerTypeMenu(final String pColor, JPanel inputMethodPanel){
      JComboBox<String> playerTypeMenu = new JComboBox<>(playerTypeList);
      playerTypeMenu.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          JComboBox cb = (JComboBox)e.getSource();
          String playerType = (String)cb.getSelectedItem();
          switch (pColor){
            case "red" :
            redPlayerType = playerType;
            break;
            case "blue" :
            bluePlayerType = playerType;
            break;
            case "my" :
            myPlayerType = playerType;
            break;
          }
          if (playerType.equals("Human")){
            inputMethodPanel.setVisible(true);
          }
          else{
            inputMethodPanel.setVisible(false);
          }
        };
      });
      return playerTypeMenu;
    }

    /**
    *This method creates a panel to select input method.
    *@param pColor
    *       Takes "red", "blue", or "my" as argument.
    */
    private JPanel createInputMethodPanel(final String pColor){
      JPanel inputMethodPanel = new JPanel();
      inputMethodPanel.setLayout(new BoxLayout(inputMethodPanel, BoxLayout.Y_AXIS));
      JRadioButton textInputButton = new JRadioButton("Read in move from text");
      textInputButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          switch (pColor){
            case "red" :
            redTextInputEnabled = true;
            break;
            case "blue" :
            blueTextInputEnabled = true;
            break;
            case "my" :
            myTextInputEnabled = true;
            break;
          }
        };
      });

      JRadioButton graphicalInputButton = new JRadioButton("Read in move from GUI");
      graphicalInputButton.setSelected(true);
      graphicalInputButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e){
          switch (pColor){
            case "red" :
            redTextInputEnabled = false;
            break;
            case "blue" :
            blueTextInputEnabled = false;
            break;
            case "my" :
            myTextInputEnabled = false;
            break;
          }
        };
      });
      ButtonGroup inputButtonGroup = new ButtonGroup();
      inputButtonGroup.add(textInputButton);
      inputButtonGroup.add(graphicalInputButton);

      inputMethodPanel.add(graphicalInputButton);
      inputMethodPanel.add(textInputButton);
      return inputMethodPanel;
    }
}
