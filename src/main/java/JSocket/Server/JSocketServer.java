package JSocket.Server;

import JSocket.Server.Abstract.Connection;
import JSocket.Server.Abstract.Handleable;
import JSocket.Server.Exceptions.ConnectionException;
import JSocket.Server.Utility.BasicConnection;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JSocketServer implements Closeable,AutoCloseable {
    private final ServerSocket server;
    private final Handleable handler;
    private Connection connection;
    private ThreadPoolExecutor tpe;
    public JSocketServer(Handleable handler, int port) throws ConnectionException {
        try {
            this.server = new ServerSocket(port);
            this.handler = handler;
            this.connection = new BasicConnection();
        }
        catch (IOException e){
            throw new ConnectionException("Could not open Socket on port " + String.valueOf(port));
        }
    }

    /**
     *
     * @param connectionHandler implementation of an interface Handleable. Contains method handle which is called when client connects.
     * @param port port on which server should operate.
     * @param connection this argument should be passed as Connection implementation without client or connectionHandler set.
     * @throws ConnectionException
     */
    public JSocketServer(Handleable connectionHandler, int port, Connection connection) throws ConnectionException {
        this(connectionHandler, port);
        this.connection = connection;
    }
    public void runAsynchronously(int numberOfThreads) throws ConnectionException {
        this.tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
        while (true){
            try {
                Connection c = (Connection) this.connection.clone();
                c.setHandler(this.handler);
                c.setClient(this.server.accept());
                tpe.execute(c);
            }
            catch (IOException e){
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
            c.setHandler(this.handler);
            c.setClient(this.server.accept());
            c.run();
        } catch (IOException e) {
            throw new ConnectionException("Could not sustain connection with the client");
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void close() throws IOException {
        this.server.close();
        if(this.tpe != null) {
            this.tpe.shutdown();
        }
    }
}