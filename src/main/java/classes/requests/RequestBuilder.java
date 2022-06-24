package classes.requests;

import java.util.ArrayList;
import java.util.List;

public class RequestBuilder {

    private final List<String> headers;
    private boolean isHeadersFinished;
    private String body;

    public RequestBuilder() {
        this.headers = new ArrayList<>();
    }

    public Request build() {
        return new Request(this.headers, this.body);
    }

    public RequestBuilder addString(String string) {
        if (string.isEmpty()) {
            this.isHeadersFinished = true;
        } else if (!this.isHeadersFinished) {
            this.headers.add(string);
        } else {
            this.body = string;
        }
        return this;
    }
}
