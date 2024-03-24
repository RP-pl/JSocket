package JSocket.Client;

import JSocket.Client.Exceptions.ProtocolSwitchException;
import JSocket.Client.Utility.BasicConnectionSwitchHandler;
import JSocket.Client.Abstract.ConnectionSwitchHandler;
import JSocket.Common.IO.ConnectionIO;

import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JSocketClient implements Closeable, AutoCloseable {
    final Socket socket;
    final ConnectionIO connectionIO;

    public JSocketClient(String host, int port, String connectionEndpoint, ConnectionSwitchHandler switchHandler, boolean ssl) throws IOException, ProtocolSwitchException {
        if(host == null || host.isEmpty())
            throw new IllegalArgumentException("Host cannot be null or empty");
        if(port < 0 || port > 65535)
            throw new IllegalArgumentException("Port must be between 0 and 65535");
        if (ssl)
            this.socket = createSSLSocket(host, port);
        else
            this.socket = new Socket(host, port);
        this.switchProtocolToWebsocket(switchHandler, connectionEndpoint, ssl);
        connectionIO = new ConnectionIO(this.socket);
    }


    public JSocketClient(String host, int port, String connectionEndpoint, boolean ssl) throws IOException, ProtocolSwitchException {
        this(host, port, connectionEndpoint, new BasicConnectionSwitchHandler(), ssl);
    }

    public JSocketClient(String host, int port, String connectionEndpoint) throws IOException, ProtocolSwitchException {
        this(host, port, connectionEndpoint, new BasicConnectionSwitchHandler(), false);
    }

    public JSocketClient(String host, int port, ConnectionSwitchHandler switchHandler) throws IOException, ProtocolSwitchException {
        this(host, port, "/", switchHandler, false);
    }

    public JSocketClient(String host, int port, boolean ssl) throws IOException, ProtocolSwitchException {
        this(host, port, "/", new BasicConnectionSwitchHandler(), ssl);
    }

    public JSocketClient(URI connection) throws IOException, ProtocolSwitchException, URISyntaxException {
        this(connection, new BasicConnectionSwitchHandler());
    }

    public JSocketClient(URI connection, ConnectionSwitchHandler handler) throws IOException, ProtocolSwitchException, URISyntaxException {
        if (!connection.getScheme().equals("ws") && !connection.getScheme().equals("wss"))
            throw new ProtocolSwitchException("Invalid Protocol. Only ws and wss are supported.");

        boolean ssl = connection.getScheme().equals("wss");
        if (ssl)
            this.socket = createSSLSocket(connection.getHost(), connection.getPort());
        else
            this.socket = new Socket(connection.getHost(), connection.getPort());
        this.switchProtocolToWebsocket(handler, connection.getPath().isEmpty() ? "/" : connection.getPath(), ssl);
        connectionIO = new ConnectionIO(this.socket);
    }

    public JSocketClient(String connection) throws IOException, ProtocolSwitchException, URISyntaxException {
        this(new URI(connection));
    }

    public JSocketClient(String host, int port) throws IOException, ProtocolSwitchException {
        this(host, port, "/", new BasicConnectionSwitchHandler(), false);
    }

    private void switchProtocolToWebsocket(ConnectionSwitchHandler connectionSwitchHandler, String connectionEndpoint, boolean ssl) throws IOException, ProtocolSwitchException {
        connectionSwitchHandler.switchProtocol(this.socket, connectionEndpoint, ssl);
    }

    /**
     * Get an object responsible for data exchange between Server and Client
     *
     * @return ConnectionIO
     */
    public ConnectionIO getConnectionIO() {
        return connectionIO;
    }

    private Socket createSSLSocket(String host, int port) throws IOException {
        return SSLSocketFactory.getDefault().createSocket(host, port);
    }

    @Override
    public void close() throws IOException {
        this.connectionIO.close();
    }
}
