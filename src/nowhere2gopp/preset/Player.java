package nowhere2gopp.preset;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Player extends Remote {
    Move request() throws Exception, RemoteException;

    void confirm(Status status) throws Exception, RemoteException;

    void update(Move   opponentMove,
                Status status) throws Exception, RemoteException;

    void init(int         boardSize,
              PlayerColor color) throws Exception, RemoteException;
}
