package JSocket.Exceptions;

/**
 * This exception is thrown when client is unable to connect to the server
 */
public class ConnectionException extends Exception{
    public ConnectionException(String s) {
        super(s);
    }
}
