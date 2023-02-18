package JSocket.Interfaces;

import JSocket.Exceptions.ConnectionCloseException;
import JSocket.IO.ConnectionIO;
import JSocket.IO.DataFrameInputStream;
import JSocket.IO.DataFrameOutputStream;

public interface Handleable{
    public void handle(ConnectionIO io) throws ConnectionCloseException;
}
