package JSocket.Server.Abstract;

import JSocket.Server.Exceptions.ConnectionCloseException;
import JSocket.Server.IO.ConnectionIO;


/**
 * This interface is used by the Connection class to handle data exchange between Server and Client
 */
public interface Handleable{
    public void handle(ConnectionIO io) throws ConnectionCloseException;
}
