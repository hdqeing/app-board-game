package nowhere2gopp.gamelogic.player;

import nowhere2gopp.preset.*;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;

import nowhere2gopp.gamelogic.GameBoard;
import nowhere2gopp.preset.Move;
import nowhere2gopp.preset.Player;
import nowhere2gopp.preset.PlayerColor;
import nowhere2gopp.preset.Status;

/**
 * This class can be used to create online player.
 */
public class NetworkPlayer extends UnicastRemoteObject implements Player {
    // --------------------------Attributes-----------

    /**
     * A local player used to create network player which has the same player type as network player.
     */
    private Player remotePlayer;

    // --------------------------Constructor----------
    public NetworkPlayer(final Player newPlayer) throws RemoteException {
        remotePlayer = newPlayer;
    }

    // --------------------------Methods--------------

    /**
     * Request move.
     */
    public Move request() throws Exception, RemoteException {
        Move move = remotePlayer.request();

        return move;
    }

    /**
     * Confirm status.
     */
    public void confirm(final Status status) throws Exception, RemoteException {
        remotePlayer.confirm(status);
    }

    /**
     * Update opponents game board and GUI.
     */
    public void update(final Move opponentMove, final Status status) throws Exception, RemoteException {
        remotePlayer.update(opponentMove, status);
    }

    /**
     * Initialize player. See {@link AbstractPlayer}.
     */
    public void init(final int gbdSize, final PlayerColor color) throws Exception, RemoteException {
        remotePlayer.init(gbdSize, color);
    }
}
