import classes.handlers.ForClassicRequestHandler;
import classes.server.MyServer;
import classes.handlers.ForFormRequestHandler;
import classes.handlers.MainRequestHandler;

import java.util.List;

public class Main {
    public static final String GET = "GET";
    public static final String POST = "POST";

    public static void main(String[] args) {
        final var threadsInPool = 64;
        final var maxRequestBufferInBytes = 4096;
        final var port = 8888;
        final var directory = "public";
        final var allowedMethods = List.of(GET, POST);
        final var validPaths = List.of(
                "/index.html",
                "/spring.svg",
                "/spring.png",
                "/resources.html",
                "/styles.css",
                "/app.js",
                "/links.html",
                "/forms.html",
                "/classic.html",
                "/events.html",
                "/events.js"
        );

        final var server = new MyServer(threadsInPool, maxRequestBufferInBytes, allowedMethods);

        // добавление handler'ов (обработчиков)
        for (var method : allowedMethods) {
            for (var path : validPaths) {
                if (path.startsWith("/forms.html")) {
                    server.addHandler(method, path, new ForFormRequestHandler(directory));
                } else if (path.equals("/classic.html")) {
                    server.addHandler(method, path, new ForClassicRequestHandler(directory));
                } else {
                    server.addHandler(method, path, new MainRequestHandler(directory));
                }
            }
        }
        server.listen(port);
    }
}

