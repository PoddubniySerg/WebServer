package classes.requests;

import java.util.List;

public class Request {

    private final String method, path, version, body;
    private final List<String> headers;

    public Request(String method, String path, String version, String body, List<String> headers) {
        this.method = method;
        this.path = path;
        this.version = version;
        this.body = body;
        this.headers = headers;
    }

    public String getBody() {
        return this.body;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getVersion() {
        return version;
    }

    public boolean isRequestHttp() {
        return this.method != null
                && this.path != null
                && this.version != null
                && !this.method.isEmpty()
                && !this.path.isEmpty()
                && !this.version.isEmpty()
                && this.path.startsWith("/")
                && this.version.equals("HTTP/1.1");
    }
}
