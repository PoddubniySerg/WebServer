package interfaces;

import classes.requests.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

public interface Handler {

    void handle(Request request, BufferedOutputStream responseStream) throws IOException, URISyntaxException;
}
