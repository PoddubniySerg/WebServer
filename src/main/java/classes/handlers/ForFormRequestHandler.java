package classes.handlers;

import classes.requests.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public class ForFormRequestHandler extends MainRequestHandler {
    public ForFormRequestHandler(String directory) {
        super(directory);
    }

    @Override
    public void handle(Request request, BufferedOutputStream responseStream) throws IOException, URISyntaxException {
        System.out.println();
        System.out.println("Method: " + request.getMethod());
        System.out.println("Path: " + request.getPath());
        System.out.println("Query params: " + request.getQueryParams());
        System.out.println("Post params: " + request.getPostParams());
        System.out.println();
        System.out.println("Headers:");
        request.getHeaders().forEach(System.out::println);
        System.out.println();
        System.out.println("Body:");
        System.out.println(request.getBody());
        super.handle(request, responseStream);
    }
}
