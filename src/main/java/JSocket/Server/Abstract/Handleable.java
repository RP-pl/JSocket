package JSocket.Server.Abstract;

import JSocket.Server.Exceptions.ConnectionCloseException;
import JSocket.Common.IO.ConnectionIO;


/**
 * This interface is used by the Connection class to handle data exchange between Server and Client
 */
public interface Handleable {
    void handle(ConnectionIO io) throws ConnectionCloseException;
}
