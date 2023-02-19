package JSocket;

import JSocket.Abstract.Connection;
import JSocket.Abstract.Handleable;
import JSocket.Exceptions.ConnectionException;
import JSocket.Utility.BasicConnection;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JSocket {
    private ServerSocket server;
    private Handleable handler;
    private Connection connection;
    private ThreadPoolExecutor tpe;
    public JSocket(Handleable handler,int port) throws ConnectionException {
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
     * @param handler implementation of an interface Handleable.
     * @param port port on which server should operate.
     * @param connection this argument should be passed as Connection implementation without client or handler set.
     * @throws ConnectionException
     */
    public JSocket(Handleable handler,int port,Connection connection) throws ConnectionException {
        this(handler, port);
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
    }
}
