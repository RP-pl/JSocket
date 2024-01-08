package JSocket.Server.Abstract;

import JSocket.Server.Exceptions.ConnectionRefusedException;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public abstract class Connection implements Runnable, Cloneable {
    /**
     * Implement this method to handle additional protocols during handshake.
     * In this method you can also deny any incoming connection.
     *
     * @throws IOException
     */
    protected abstract void doHandshake() throws IOException, ConnectionRefusedException;


    /**
     * Used to make handler aware of client connection.
     */
    public abstract void run();

    /**
     * Called by JSocketServer to set client socket.
     * @param client - client socket
     */
    public abstract void setClient(Socket client);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Called by JSocketServer to set endpoints.
     * @param endpoints - map of endpoints
     */
    public abstract void setEndpoints(Map<String, Handleable> endpoints);

}
