package JSocket.Client.Utility;

import JSocket.Client.Exceptions.ProtocolSwitchException;
import JSocket.Client.Abstract.ConnectionSwitchHandler;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

import static JSocket.Common.ParseUtil.parseHttpRequest;

public class BasicConnectionSwitchHandler implements ConnectionSwitchHandler {
    public BasicConnectionSwitchHandler() {
    }

    @Override
    public void switchProtocol(Socket socket,String connectionEndpoint) throws IOException, ProtocolSwitchException {

        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (Exception e){
            throw new RuntimeException(e);
        }

        UUID guid = UUID.randomUUID();
        String key = Base64.getEncoder().encodeToString(sha1.digest(guid.toString().getBytes()));
        String request = "GET " + connectionEndpoint + "HTTP 1.1\r\n" +
                         "Upgrade: websocket\r\n" +
                         "Connection: Upgrade\r\n" +
                         "Sec-WebSocket-Key: " + key +
                         "\r\nSec-WebSocket-Version: 13\r\n\r\n";
        OutputStream output = socket.getOutputStream();


        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
        output.write(requestBytes,0,requestBytes.length);
        Scanner inputScanner = new Scanner(socket.getInputStream(),StandardCharsets.UTF_8);
        inputScanner.useDelimiter("\\r\\n\\r\\n");
        Map<String,String> headers = parseHttpRequest(inputScanner.next());
        if (!headers.get("Upgrade").equals("websocket")){
            throw new ProtocolSwitchException("Server did not accept websocket protocol");
        }
    }

    @Override
    public void switchProtocol(Socket socket) throws IOException, ProtocolSwitchException {
        this.switchProtocol(socket,"/");
    }
}
