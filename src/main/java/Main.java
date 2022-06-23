import java.util.List;

public class Main {

    public static void main(String[] args) {
        final var threadsInPool = 64;
        final var port = 8888;
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

        new MyServer(port, validPaths, threadsInPool).start();
    }
}

