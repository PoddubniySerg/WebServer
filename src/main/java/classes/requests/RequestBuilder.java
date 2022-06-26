package classes.requests;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {


    private final List<String> headers;
    private String[] requestLine;
    private String body;

    public RequestBuilder() {
        this.headers = new ArrayList<>();
    }

    public Request build() throws URISyntaxException {
        return new Request(this.requestLine, this.headers, this.body);
    }

    public void addHeaders(List<String> headers) {
        if (headers != null && !headers.isEmpty()) this.headers.addAll(headers);
    }

    public void addRequestLine(String[] requestLine) {
        this.requestLine = requestLine;
    }

    public void addBody(String body) {
        this.body = body;
    }
}
