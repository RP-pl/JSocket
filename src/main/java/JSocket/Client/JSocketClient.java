package JSocket.Client;

import JSocket.Client.Exceptions.ProtocolSwitchException;
import JSocket.Client.Utility.BasicConnectionSwitchHandler;
import JSocket.Client.Abstract.ConnectionSwitchHandler;
import JSocket.Common.IO.ConnectionIO;

import java.io.IOException;
import java.net.Socket;

public class JSocketClient {
    final Socket socket;
    final ConnectionIO connectionIO;
    public JSocketClient(String host, int port,String connectionEndpoint,ConnectionSwitchHandler switchHandler) throws IOException, ProtocolSwitchException {
        this.socket = new Socket(host,port);
        this.switchProtocolToWebsocket(switchHandler,connectionEndpoint);
        connectionIO = new ConnectionIO(this.socket);
    }

    public JSocketClient(String host,int port, String connectionEndpoint) throws IOException, ProtocolSwitchException {
        this(host,port,"/",new BasicConnectionSwitchHandler());
    }

    public JSocketClient(String host,int port) throws IOException, ProtocolSwitchException {
        this(host,port,"/",new BasicConnectionSwitchHandler());
    }

    private void switchProtocolToWebsocket(ConnectionSwitchHandler connectionSwitchHandler, String connectionEndpoint) throws IOException, ProtocolSwitchException {
        connectionSwitchHandler.switchProtocol(this.socket, connectionEndpoint);
    }

    /**
     *
     * Get an object responsible for data exchange between Server and Client
     * @return ConnectionIO
     */
    public ConnectionIO getConnectionIO() {
        return connectionIO;
    }
}
