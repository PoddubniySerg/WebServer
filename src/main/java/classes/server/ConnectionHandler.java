package classes.server;

import classes.requests.RequestBuilder;
import interfaces.Handler;
import org.apache.commons.fileupload.FileUploadException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public class ConnectionHandler {

    private final int maxRequestBufferInBytes;

    public ConnectionHandler(int maxRequestBufferInBytes) {
        this.maxRequestBufferInBytes = maxRequestBufferInBytes;
    }

    public void connectClient(Socket socket, ConcurrentMap<String, Handler> handlers, List<String> allowedMethods) {
        try (
                socket;
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            in.mark(maxRequestBufferInBytes);
            final var buffer = new byte[maxRequestBufferInBytes];
            final var read = in.read(buffer);
            final var requestBuilder = new RequestBuilder();

//                find request line end byte
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }

//                get request line
            final var requestLine = getRequestLine(buffer, requestLineEnd, allowedMethods);
            if (requestLine == null) {
                badRequest(out);
                return;
            }
            requestBuilder.addRequestLine(requestLine);

//                get headers
            in.reset();
            final var headersStart = (int) in.skip(requestLineEnd + 2);
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var hedersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
            final var headersBytes = in.readNBytes(hedersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));
            requestBuilder.addHeaders(headers);

//                if the requestget is not GET find the body
            if (!requestLine[0].equals("GET")) {
                in.readNBytes(headersDelimiter.length);
                final var contentLength = extractContentLengthHeader(headers);
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    final var body = new String(bodyBytes);
                    requestBuilder.addBody(body);
                }
            }

//                build request
            final var request = requestBuilder.build();

            if (!handlers.containsKey(request.getMethod() + request.getPath())) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

//                handle request
            handlers.get(request.getMethod() + request.getPath()).handle(request, out);

        } catch (IOException | URISyntaxException | FileUploadException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getRequestLine(byte[] buffer, int requestLineEnd, List<String> allowedMethods) {

//                read request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) return null;

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) return null;

        return requestLine;
    }

    private Optional<String> extractContentLengthHeader(List<String> headers) {
        return headers.stream()
                .filter(o -> o.startsWith("Content-Length"))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private int indexOf(byte[] array, byte[] taget, int start, int max) {
        outer:
        for (int i = start; i < max - taget.length + 1; i++) {
            for (int j = 0; j < taget.length; j++) {
                if (array[i + j] != taget[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }
}
