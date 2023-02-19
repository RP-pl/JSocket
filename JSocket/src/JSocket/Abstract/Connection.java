package JSocket.Abstract;

import java.io.IOException;
import java.net.Socket;

public abstract class Connection implements Runnable,Cloneable {
    /**
     * Implement this method to handle additional protocols during handshake.
     * In this method you can also deny any incoming connection.
     * @throws IOException
     */
    protected abstract void doHandshake() throws IOException;

    /**
     * This method allows you to pass some initial data to the handler.
     * It has to call {@code  handler.handle(connection);}
     */
    public abstract void run();
    public abstract void setHandler(Handleable handler);
    public abstract void setClient(Socket client);

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
