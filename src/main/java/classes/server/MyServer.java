package classes.server;

import interfaces.Handler;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {

    private final ConcurrentMap<String, Handler> handlers;
    private final ExecutorService threadPool;
    private final int maxRequestBufferInBytes;
    private final List<String> allowedMethods;

    public MyServer(int threadsInPool, int maxRequestBufferInBytes, List<String> allowedMethods) {
        this.handlers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(threadsInPool);
        this.maxRequestBufferInBytes = maxRequestBufferInBytes;
        this.allowedMethods = allowedMethods;
    }

    public void listen(int port) {
        try (
                final var serverSocket = new ServerSocket(port)
        ) {
            while (!serverSocket.isClosed()) {
                var newClient = serverSocket.accept();
                threadPool.execute(() ->
                        new ConnectionHandler(maxRequestBufferInBytes).connectClient(newClient, handlers, allowedMethods));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        this.handlers.put(method + path, handler);
    }
}
