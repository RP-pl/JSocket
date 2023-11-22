package JSocket.Client.Abstract;

import JSocket.Client.Exceptions.ProtocolSwitchException;

import java.io.IOException;
import java.net.Socket;

public interface ConnectionSwitchHandler {

    /**
     * This method is used to switch the protocol of the socket to websocket
     * @param socket - Socket to switch the protocol of
     * @param connectionEndpoint - An endpoint to which client should connect to
     */
    public void switchProtocol(Socket socket,String connectionEndpoint) throws IOException, ProtocolSwitchException;

    public void switchProtocol(Socket socket) throws IOException, ProtocolSwitchException;
}
