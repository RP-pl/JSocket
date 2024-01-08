package JSocket.Server.Utility;

import JSocket.Server.Abstract.Connection;
import JSocket.Server.Exceptions.ConnectionCloseException;
import JSocket.Common.IO.ConnectionIO;
import JSocket.Server.Abstract.Handleable;
import JSocket.Server.Exceptions.ConnectionRefusedException;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static JSocket.Common.ParseUtil.parseHttpRequest;

/**
 * Basic implementation of initial WebSocket connection establishment.
 * This implementation handles no additional protocols.
 * Prone to Denial-of-Service attack.
 */
public class BasicConnection extends Connection {
    private Map<String, Handleable> endpoints;
    private Socket client;
    private MessageDigest sha1;
    private InputStream input;
    private OutputStream output;
    private final Pattern pathVariableRegex =  Pattern.compile("/.*\\{[a-zA-Z0-9]+\\}/.*");
    private final Map<String,String> pathVariables = new HashMap<>();
    private Handleable handler;

    public BasicConnection() {
        try {
            this.sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ignored) {
        }
    }

    @Override
    public void run() {
        try {
            this.input = client.getInputStream();
            this.output = client.getOutputStream();
            try{
                doHandshake();
                handler.handle(new ConnectionIO(this.client), this.pathVariables);
            } catch (ConnectionCloseException | ConnectionRefusedException e) {
                this.client.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doHandshake() throws IOException, ConnectionRefusedException {
        Scanner inputScanner = new Scanner(this.input, StandardCharsets.UTF_8);
        inputScanner.useDelimiter("\\r\\n\\r\\n");
        Map<String, String> headers = parseHttpRequest(inputScanner.next());
        this.handler = getHandler(headers.get("endpoint"));
        System.out.println(this.handler);
        byte[] response;
        if (this.handler == null) {
            response = (
                    "HTTP/1.1 406 Not Acceptable\r\n"+
                    "Content-Type: text/plain\r\n"+
                    "Content-Length: 14\r\n"+
                    "Not Acceptable\r\n\r\n"
            ).getBytes(StandardCharsets.UTF_8);
            this.output.write(response, 0, response.length);
            throw new ConnectionRefusedException("No endpoint found");
        }
        else {
            String key = getWebSocketKey(headers);
            response = ("HTTP/1.1 101 Switching Protocols\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Accept: " + key + "\r\n" + "\r\n"
            ).getBytes(StandardCharsets.UTF_8);
            this.output.write(response, 0, response.length);
        }
    }


    private Handleable getHandler(String endpoint) {

        for(String key : this.endpoints.keySet()) {
            Matcher m = this.pathVariableRegex.matcher(key);
            if(m.matches()){
                String[] path = key.split("/");
                String[] endpointPath = endpoint.split("/");
                if(path.length != endpointPath.length){
                    continue;
                }
                for(int i = 0; i < path.length; i++){
                    if(path[i].equals(endpointPath[i])){
                        continue;
                    }
                    if(path[i].startsWith("{") && path[i].endsWith("}")){
                        this.pathVariables.put(path[i].substring(1,path[i].length()-1),endpointPath[i]);
                        continue;
                    }
                    break;
                }
                return this.endpoints.get(key);
            }
        }
        return this.endpoints.getOrDefault(endpoint, this.endpoints.get("/"));
    }
    private String getWebSocketKey(Map<String, String> headers) {
        String clientKey = headers.get("Sec-WebSocket-Key");
        String key = clientKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        byte[] hash = this.sha1.digest(key.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public void setClient(Socket client) {
        this.client = client;
    }

    @Override
    public void setEndpoints(Map<String, Handleable> endpoints) {
        this.endpoints = endpoints;
    }

}
