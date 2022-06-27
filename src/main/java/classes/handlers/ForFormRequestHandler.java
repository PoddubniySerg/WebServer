package classes.handlers;

import classes.requests.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class ForFormRequestHandler extends MainRequestHandler {
    public ForFormRequestHandler(String directory) {
        super(directory);
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException, URISyntaxException {
        System.out.println();
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getPath());
        System.out.println("Params: " + request.getQueryParams());
        System.out.println();
        System.out.println("Headers:");
        request.getHeaders().forEach(System.out::println);
        System.out.println();
        System.out.println("Body:");
        if (request.getParts().isEmpty()) {
            System.out.println(request.getBody());
            System.out.println();
        }
        request.getParts().forEach(part -> System.out.println(part.getFieldName() + ": " + new String(part.get(), StandardCharsets.UTF_8)));
        super.handle(request, responseStream);
    }
}
