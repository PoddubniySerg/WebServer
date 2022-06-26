package classes.requests;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private final String method;
    private final String path;
    private final List<NameValuePair> params;
    private final List<String> headers;
    private final String body;

    public Request(String[] requestLine, List<String> headers, String body) throws URISyntaxException {
        this.params = new ArrayList<>();
        this.method = requestLine[0];
        this.headers = headers;
        this.body = body;
        if (requestLine[1].contains("?")) {
            this.path = requestLine[1].substring(0, requestLine[1].indexOf('?'));
            this.params.addAll(URLEncodedUtils.parse(new URI(requestLine[1]), StandardCharsets.UTF_8));
        } else {
            this.path = requestLine[1];
        }
        if (headers.stream()
                .filter(header -> header.startsWith("Content-Type"))
                .anyMatch(header -> header.contains("x-www-form-urlencoded"))
                && this.body != null
                && !this.body.isEmpty()
        ) {
            this.params.addAll(URLEncodedUtils.parse(this.body, StandardCharsets.UTF_8));
        }
    }

    public String getBody() {
        return this.body;
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public List<String> getHeaders() {
        return this.headers;
    }

    public List<NameValuePair> getQueryParams() {
        return this.params.stream().sorted(Comparator.comparing(NameValuePair::getName)).collect(Collectors.toList());
    }

    public List<NameValuePair> getQueryParam(String name) {
        return this.params.stream().filter(param -> param.getName().equals(name)).collect(Collectors.toList());
    }
}
