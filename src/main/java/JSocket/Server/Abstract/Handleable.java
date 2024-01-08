package JSocket.Server.Abstract;

import JSocket.Server.Exceptions.ConnectionCloseException;
import JSocket.Common.IO.ConnectionIO;
import java.util.Map;


/**
 * This interface is used by the Connection class to handle data exchange between Server and Client
 */
public interface Handleable {

    /**
     * This method is called by the Connection class to handle data exchange between Server and Client
     * @param io The ConnectionIO object used to communicate with the Client
     * @param pathVariables The path variables extracted from the request
     * @throws ConnectionCloseException If the connection is closed by the Client
     */
    void handle(ConnectionIO io, Map<String,String> pathVariables) throws ConnectionCloseException;
}
