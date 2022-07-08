package classes.handlers;

import classes.requests.Request;
import interfaces.Handler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MainRequestHandler implements Handler {

    protected final String directory;

    public MainRequestHandler(String directory) {
        this.directory = directory;
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        Path filePath = Path.of(this.directory + request.getPath());
        final var mimeType = Files.probeContentType(filePath);
        final var length = Files.size(filePath);
        responseStream.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, responseStream);
        responseStream.flush();
    }
}
