package classes;

import classes.requests.RequestBuilder;
import interfaces.Handler;
import org.apache.commons.fileupload.FileUploadException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class MyServer {

    private final ConcurrentMap<String, Handler> handlers;
    private final ExecutorService threadPool;
    private final int maxRequestBufferInBytes;
    private final List<String> allowedMethods;

    public MyServer(int threadsInPool, int maxRequestBufferInBytes, List<String> allowedMethods) {
        this.handlers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(threadsInPool);
        this.maxRequestBufferInBytes = maxRequestBufferInBytes;
        this.allowedMethods = allowedMethods;
    }

    public void listen(int port) {
        try (
                final var serverSocket = new ServerSocket(port)
        ) {
            while (!serverSocket.isClosed()) {
                var newClient = serverSocket.accept();
                this.threadPool.execute(() -> connectClient(newClient));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        this.handlers.put(method + path, handler);
    }

    private void connectClient(Socket socket) {
        try (
                socket;
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            in.mark(this.maxRequestBufferInBytes);
            final var buffer = new byte[this.maxRequestBufferInBytes];
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
            final var requestLine = getRequestLine(buffer, requestLineEnd);
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

            if (!this.handlers.containsKey(request.getMethod() + request.getPath())) {
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
            this.handlers.get(request.getMethod() + request.getPath()).handle(request, out);

        } catch (IOException | URISyntaxException | FileUploadException e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getRequestLine(byte[] buffer, int requestLineEnd) {

//                read request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) return null;

        final var method = requestLine[0];
        if (!this.allowedMethods.contains(method)) return null;

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
