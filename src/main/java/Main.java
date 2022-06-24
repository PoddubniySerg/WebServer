import classes.handlers.ForClassicRequestHandler;
import classes.MyServer;
import classes.handlers.MainRequestHandler;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        final var threadsInPool = 64;
        final var port = 8888;
        final var directory = "public";
        final var methods = List.of("GET", "POST");
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

        final var server = new MyServer(threadsInPool);

        // добавление handler'ов (обработчиков)
        for (var method : methods) {
            for (var path : validPaths) {
                server.addHandler(method,
                        path,
                        path.equals("/classic.html") ? new ForClassicRequestHandler(directory) : new MainRequestHandler(directory));
            }
        }
        server.listen(port);
    }
}

