package JSocket.Server.Exceptions;

/**
 * This exception is thrown when an unknown OPCode is received
 */
public class UnknownOPCodeException extends Exception{
    public UnknownOPCodeException(String s){
        super(s);
    }
}
