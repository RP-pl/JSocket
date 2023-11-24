# JSocket
Simple Java WebSocket Server and Client capable of asynchronous work.
Supports multiple clients and multiple threads.
## Usage
### Server
```java
Handleable defaultHandler = (io, message) -> {
    System.out.println("Received message: " + message);
    io.writeString("Hello from server!");
};

Handleable endpointHandler = (io, message) -> {
    System.out.println("Received message: " + message);
    io.writeString("Hello from /test!");
};
        
JSocketServer server = new JSocketServer(8080,defaultHandler);
server.addEndpoint("/test", endpointHandler);
server.runSynchronously();
```

### Client
```java
JSocketClient client = new JSocketClient("ws://localhost:8080");
ConnectionIO io = client.getConnectionIO();
io.writeString("Hello World!");
System.out.println(io.readString(false));
io.close();
```

## Additional protocols

This library can be easily extended with additional protocol support by implementing the `Connection` interface for Server and `ConnectionSwitchHandler` for Client.

