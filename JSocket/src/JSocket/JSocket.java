package JSocket;

import JSocket.Interfaces.Handleable;
import JSocket.Exceptions.ConnectionException;
import JSocket.Utility.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JSocket {
    private ServerSocket server;
    private Handleable handler;
    private ThreadPoolExecutor tpe;
    public JSocket(Handleable handler,int port) throws ConnectionException {
        try {
            this.server = new ServerSocket(port);
            this.handler = handler;
        }
        catch (IOException e){
            throw new ConnectionException("Could not open Socket on port " + String.valueOf(port));
        }
    }
    public void runAsynchronously(int numberOfThreads) throws ConnectionException {
        this.tpe = (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfThreads);
        while (true){
            try {
                tpe.execute(new Connection(this.server.accept(), handler));
            }
            catch (IOException e){
                throw new ConnectionException("Could not sustain connection with the client");
            }
        }
    }
    public void runSynchronously() throws ConnectionException {
        while (true) {
            try {
                (new Connection(this.server.accept(), handler)).run();
            } catch (IOException e) {
                throw new ConnectionException("Could not sustain connection with the client");
            }
        }
    }
}
