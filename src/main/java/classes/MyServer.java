package classes;

import classes.requests.RequestBuilder;
import interfaces.Handler;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class MyServer {

    private final ConcurrentMap<String, Handler> handlers;

    private final ExecutorService threadPool;

    public MyServer(int threadsInPool) {
        this.handlers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(threadsInPool);
    }

    public void listen(int port) {
        try (
                final var serverSocket = new ServerSocket(port)
        ) {
            while (!serverSocket.isClosed()) {
                var newClient = serverSocket.accept();
                this.threadPool.execute(() -> connectClient(newClient));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        this.handlers.put(method + path, handler);
    }

    private void connectClient(Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            final var requestBuilder = new RequestBuilder();
            while (in.ready()) {
                requestBuilder.addString(in.readLine());
            }
            final var request = requestBuilder.build();

            if (!request.isRequestHttp()) {
                // just close socket
                return;
            }

            if (!this.handlers.containsKey(request.getMethod() + request.getPath())) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            this.handlers.get(request.getMethod() + request.getPath()).handle(request, out);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
