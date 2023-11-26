package JSocket.Server;

import JSocket.Server.Abstract.Connection;
import JSocket.Server.Abstract.Handleable;
import JSocket.Server.Exceptions.ConnectionException;
import JSocket.Server.Utility.BasicConnection;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JSocketServer implements Closeable, AutoCloseable {
    private final ServerSocket server;
    private final Connection connection;
    private ThreadPoolExecutor tpe;

    private final Map<String, Handleable> endpoints = Collections.synchronizedMap(new TreeMap<>());

    public JSocketServer(Handleable defaultHandler, int port) throws ConnectionException {
        this(defaultHandler, port, new BasicConnection(), false);
    }


    public JSocketServer(Handleable defaultHandler, int port, boolean ssl) throws ConnectionException {
        this(defaultHandler, port, new BasicConnection(), ssl);
    }


    /**
     * @param defaultHandler implementation of an interface Handleable. Contains method handle which is called when client connects.
     * @param port           port on which server should operate.
     * @param connection     this argument should be passed as Connection implementation without client or connectionHandler set.
     * @param ssl            indicates whether the server should use SSL
     * @throws ConnectionException
     */
    public JSocketServer(Handleable defaultHandler, int port, Connection connection, boolean ssl) throws ConnectionException {
        try {
            if (ssl)
                this.server = getSSLServerSocket(port);
            else
                this.server = new ServerSocket(port);
            this.connection = connection;
            this.endpoints.put("/", defaultHandler);
        } catch (IOException e) {
            throw new ConnectionException("Could not open Socket on port " + port);
        }
    }

    public JSocketServer(Handleable defaultHandler, int port, Connection connection) throws ConnectionException {
        this(defaultHandler, port, connection, false);
    }

    public JSocketServer(int port, Connection connection) throws ConnectionException {
        this(null, port, connection, false);
    }

    public JSocketServer(int port, Connection connection, boolean ssl) throws ConnectionException {
        this(null, port, connection, ssl);
    }

    public JSocketServer(int port) throws ConnectionException {
        this(null, port, new BasicConnection(), false);
    }


    public void runAsynchronously(int numberOfThreads) throws ConnectionException {
        this.tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
        while (true) {
            try {
                Connection c = (Connection) this.connection.clone();
                c.setClient(this.server.accept());
                tpe.execute(c);
            } catch (IOException e) {
                throw new ConnectionException("Could not sustain connection with the client");
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void runSynchronously() throws ConnectionException {
        while (true) {
            this.runOnce();
        }
    }

    public void runOnce() throws ConnectionException {
        try {
            Connection c = (Connection) this.connection.clone();
            c.setClient(this.server.accept());
            c.setEndpoints(this.endpoints);
            c.run();
        } catch (IOException e) {
            throw new ConnectionException("Could not sustain connection with the client");
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds endpoint to the server. Endpoints should be added before server is started.
     *
     * @param endpoint - endpoint to be added
     * @param handler  - implementation of Handleable interface
     */
    public void addEndpoint(String endpoint, Handleable handler) {
        this.endpoints.put(endpoint, handler);
    }

    @Override
    public void close() throws IOException {
        this.server.close();
        if (this.tpe != null) {
            this.tpe.shutdown();
        }
    }

    private ServerSocket getSSLServerSocket(int port) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
        socket.setNeedClientAuth(false);
        return socket;
    }
}
