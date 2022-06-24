package classes.requests;

import java.util.List;

public class Request {

    private final List<String> headers;
    private final String body;

    public Request(List<String> headers, String body) {
        this.headers = headers;
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }

    public String getMethod() {
        return this.headers.get(0).split(" ")[0];
    }

    public String getPath() {
        return this.headers.get(0).split(" ")[1];
    }

    public boolean isRequestHttp() {
        return !this.headers.isEmpty()
                && this.headers.get(0).split(" ").length == 3
                && this.headers.get(0).split(" ")[2].equals("HTTP/1.1");
    }
}
