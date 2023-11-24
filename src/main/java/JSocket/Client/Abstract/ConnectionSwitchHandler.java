package JSocket.Client.Abstract;

import JSocket.Client.Exceptions.ProtocolSwitchException;

import java.io.IOException;
import java.net.Socket;

public interface ConnectionSwitchHandler {

    /**
     * This method is used to switch the protocol of the socket to websocket
     *
     * @param socket             - Socket to switch the protocol of
     * @param connectionEndpoint - An endpoint to which client should connect to
     */
    void switchProtocol(Socket socket, String connectionEndpoint, boolean ssl) throws IOException, ProtocolSwitchException;

    void switchProtocol(Socket socket) throws IOException, ProtocolSwitchException;

    void switchProtocol(Socket socket, boolean ssl) throws IOException, ProtocolSwitchException;
}
