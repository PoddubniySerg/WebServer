package classes.requests;

import java.util.List;

public class Request {

    private final String requestLine;
    private final List<String> headers;
    private final String body;

    public Request(String requestLine, List<String> headers, String body) {
        this.requestLine = requestLine;
        this.headers = headers;
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return this.requestLine.split(" ")[0];
    }

    public String getPath() {
        return this.requestLine.split(" ")[1];
    }

    public boolean isRequestHttp() {
        return !this.requestLine.isEmpty()
                && this.requestLine.split(" ").length == 3
                && this.requestLine.split(" ")[2].equals("HTTP/1.1");
    }
}
