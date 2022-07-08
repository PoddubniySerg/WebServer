package classes.requests;

import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    private final List<String> headers;
    private boolean isHeadersFinished, isRequestLineGot;
    private String method, path, version, body;

    public RequestBuilder() {
        this.headers = new ArrayList<>();
    }

    public Request build() {
        return new Request(this.method, this.path, this.version, this.body, this.headers);
    }

    public void addString(String string) {
        if (!this.isRequestLineGot) {
            if (string != null && !string.isEmpty()) {
                final var array = string.split(" ");
                if (array.length == 3) {
                    this.method = array[0];
                    this.path = array[1];
                    this.version = array[2];
                } else {
                    nullRequestLine();
                }
            } else {
                nullRequestLine();
            }
            this.isRequestLineGot = true;
        } else if (string.isEmpty()) {
            this.isHeadersFinished = true;
        } else if (!this.isHeadersFinished) {
            this.headers.add(string);
        } else {
            this.body = string;
        }
    }

    private void nullRequestLine() {
        this.method = null;
        this.path = null;
        this.version = null;
    }
}
