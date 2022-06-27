package classes.requests;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;


import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Request {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final String method;
    private final String path;
    private final List<NameValuePair> params;
    private final List<String> headers;
    List<FileItem> parts;
    private final String body;

    @SuppressWarnings("deprecation")
    public Request(String[] requestLine, List<String> headers, String body) throws URISyntaxException, FileUploadException {
        this.params = new ArrayList<>();
        this.parts = new ArrayList<>();
        this.method = requestLine[0];
        this.headers = headers;
        this.body = body;
        if (requestLine[1].contains("?")) {
            this.path = requestLine[1].substring(0, requestLine[1].indexOf('?'));
            this.params.addAll(URLEncodedUtils.parse(new URI(requestLine[1]), DEFAULT_CHARSET));
        } else {
            this.path = requestLine[1];
        }
        final var contentType = this.getContentTypeHeader();
        if (contentType != null
                && !contentType.isEmpty()
                && this.body != null
                && !this.body.isEmpty()
        ) {
            if (contentType.contains("x-www-form-urlencoded")) {
                this.params.addAll(URLEncodedUtils.parse(this.body, DEFAULT_CHARSET));
            }
            if (contentType.contains("multipart/form-data")) {
                this.parts.addAll(this.parseMultipart(contentType.substring(contentType.indexOf(" ") + 1)));
            }
        }
    }

    private String getContentTypeHeader() {
        return this.headers.stream()
                .filter(header -> header.startsWith("Content-Type"))
                .findFirst().orElse(null);
    }

    private List<FileItem> parseMultipart(String contentType) throws FileUploadException {
        final var parameterParser = new ParameterParser();
        parameterParser.setLowerCaseNames(true);
        final var charset = parameterParser.parse(contentType, ';').get("charset");
        final var requestContext = new RequestContextImpl(
                charset != null && !charset.isEmpty() ? charset : DEFAULT_CHARSET.displayName(),
                contentType,
                this.body.getBytes());
        if (ServletFileUpload.isMultipartContent(requestContext)) {
            final var fileUploadBase = new PortletFileUpload();
            final var fileItemFactory = new DiskFileItemFactory();
            fileUploadBase.setFileItemFactory(fileItemFactory);
            fileUploadBase.setHeaderEncoding(requestContext.getCharacterEncoding());
            return fileUploadBase.parseRequest(requestContext);
        }
        return new ArrayList<>();
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

    public NameValuePair getQueryParam(String name) {
        return this.params.stream().filter(param -> param.getName().equals(name)).findFirst().orElse(null);
    }

    public List<FileItem> getParts() {
        return this.parts;
    }

    public FileItem getPart(String name) {
        return this.parts.stream().filter(part -> part.getFieldName().equals(name)).findFirst().orElse(null);
    }
}
