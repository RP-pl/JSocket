package JSocket.Interfaces;

import JSocket.Exceptions.ConnectionCloseException;
import JSocket.IO.ConnectionIO;


/**
 * This interface is used by the Connection class to handle data exchange between Server and Client
 */
public interface Handleable{
    public void handle(ConnectionIO io) throws ConnectionCloseException;
}
