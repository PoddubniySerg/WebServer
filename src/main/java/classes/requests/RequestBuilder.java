package classes.requests;

import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    private final List<String> headers;
    private boolean isHeadersFinished, isRequestLineGot;
    private String requestLine, body;

    public RequestBuilder() {
        this.headers = new ArrayList<>();
    }

    public Request build() {
        return new Request(this.requestLine, this.headers, this.body);
    }

    public void addString(String string) {
        if (!this.isRequestLineGot) {
            this.requestLine = string;
            this.isRequestLineGot = true;
        } else if (string.isEmpty()) {
            this.isHeadersFinished = true;
        } else if (!this.isHeadersFinished) {
            this.headers.add(string);
        } else {
            this.body = string;
        }
    }
}
