package JSocket.Utility;

import JSocket.Abstract.Connection;
import JSocket.Exceptions.ConnectionCloseException;
import JSocket.IO.ConnectionIO;
import JSocket.Abstract.Handleable;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Basic implementation of initial WebSocket connection establishment.
 * This implementation handles no additional protocols.
 * Prone to Denial-of-Service attack.
 */
public class BasicConnection extends Connection {
    private Handleable handler;
    private Socket client;
    private MessageDigest sha1;
    private InputStream input;
    private OutputStream output;
    private static Set<String> connectedClients = Collections.synchronizedSet(new HashSet<>());

    public BasicConnection(Socket client, Handleable handler){
        this.client = client;
        this.handler = handler;
        try {
            this.sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ignored){
        }
    }
    public BasicConnection(){
        try {
            this.sha1 = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException ignored){
        }
    }
    @Override
    public void run() {
        try {
            this.input = client.getInputStream();
            this.output = client.getOutputStream();
            doHandshake();
            try {
                handler.handle(new ConnectionIO(this.client));
            }
            catch (ConnectionCloseException e){
                this.client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void doHandshake() throws IOException {
        Scanner inputScanner = new Scanner(this.input,"UTF-8");
        inputScanner.useDelimiter("\\r\\n\\r\\n");
        Map<String,String> headers = parseHttpRequest(inputScanner.next());
        //TODO Implement additional protocol handling
        String key = getWebSocketKey(headers);
        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n"+
                "Sec-WebSocket-Accept: " + key + "\r\n"+"\r\n").getBytes(StandardCharsets.UTF_8);

        this.output.write(response,0, response.length);;

    }

    private Map<String,String> parseHttpRequest(String requestData) {
        Map<String,String> headers = new HashMap<>();
        String[] reqDta = requestData.split("\r\n");
        boolean dataFlag = false;
        for (int i=1;i<reqDta.length;i++) {
            if(!reqDta[i].contains(": ")){
                dataFlag = true;
            }
            if(!dataFlag) {
                String[] kv = reqDta[i].split(": ");
                headers.put(kv[0], kv[1]);
            }
        }
        return headers;
    }
    private String getWebSocketKey(Map<String,String> headers){
        String clientKey = headers.get("Sec-WebSocket-Key");
        String key = clientKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        byte[] hash = this.sha1.digest(key.getBytes(StandardCharsets.UTF_8));
        String encodedString = Base64.getEncoder().encodeToString(hash);
        return encodedString;
    }
    @Override
    public void setClient(Socket client){
        this.client = client;
    }
    @Override
    public void setHandler(Handleable handler){
        this.handler = handler;
    }
}
