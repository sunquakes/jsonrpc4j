package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.dto.RequestDto;
import com.sunquakes.jsonrpc4j.exception.JsonRpcClientException;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import com.sunquakes.jsonrpc4j.utils.SseParser;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Client for the MCP Streamable HTTP transport. It talks to the single
 * {@code /mcp} endpoint of a server and accepts both unary JSON and SSE
 * responses.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
@Slf4j
public class JsonRpcStreamableHttpClient extends JsonRpcClient implements JsonRpcClientInterface, AutoCloseable {

    private static final String SESSION_HEADER = "Mcp-Session-Id";

    private static final String PROTOCOL_HEADER = "Mcp-Protocol-Version";

    private static final String DEFAULT_PROTOCOL_VERSION = "2025-06-18";

    private static final String DEFAULT_PATH = "/mcp";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private volatile String sessionId;

    private volatile String protocolVersion = DEFAULT_PROTOCOL_VERSION;

    public JsonRpcStreamableHttpClient(Config<Object> config) {
        super(config);
    }

    @Override
    public Object handle(String method, Object[] args) throws JsonRpcException {
        return call(method, args);
    }

    public Object call(String method, Object... args) throws JsonRpcException {
        String id = RequestUtils.getId();
        String body = requestBody(id, method, args);
        HttpResponse<InputStream> response = send(body);
        try (InputStream inputStream = response.body()) {
            if (isEventStream(response)) {
                return consumeEventStream(inputStream, id);
            }
            String data = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject message = parseMessage(data);
            if (message == null) {
                throw new JsonRpcClientException("Invalid response");
            }
            raiseIfError(message);
            return message.get("result");
        } catch (IOException e) {
            throw new JsonRpcClientException(e.getMessage());
        }
    }

    public Stream<Object> stream(String method, Object... args) throws JsonRpcException {
        String id = RequestUtils.getId();
        String body = requestBody(id, method, args);
        StreamIterator iterator = new StreamIterator(id, body);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                .onClose(iterator::close);
    }

    @Override
    public void close() {
        String currentSessionId = this.sessionId;
        if (currentSessionId == null) {
            return;
        }
        this.sessionId = null;
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint()))
                    .method("DELETE", HttpRequest.BodyPublishers.noBody())
                    .header(SESSION_HEADER, currentSessionId)
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.debug("Failed to terminate the streamable http session.", e);
        } catch (IOException | RuntimeException e) {
            log.debug("Failed to terminate the streamable http session.", e);
        }
    }

    private HttpResponse<InputStream> send(String body) throws JsonRpcException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(endpoint()))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header(PROTOCOL_HEADER, protocolVersion);
        if (sessionId != null) {
            builder.header(SESSION_HEADER, sessionId);
        }
        HttpResponse<InputStream> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new JsonRpcClientException(e.getMessage());
        } catch (IOException e) {
            throw new JsonRpcClientException(e.getMessage());
        }
        absorb(response);
        checkStatus(response);
        return response;
    }

    private void checkStatus(HttpResponse<InputStream> response) throws JsonRpcClientException {
        int status = response.statusCode();
        if (status == 200) {
            return;
        }
        if (status == 404) {
            sessionId = null;
            throw new JsonRpcClientException("Session not found");
        }
        throw new JsonRpcClientException(String.format("Request failed with status code %d", status));
    }

    private void absorb(HttpResponse<InputStream> response) {
        response.headers().firstValue(SESSION_HEADER).filter(value -> !value.isEmpty()).ifPresent(value -> sessionId = value);
        response.headers().firstValue(PROTOCOL_HEADER).filter(value -> !value.isEmpty()).ifPresent(value -> protocolVersion = value);
    }

    private boolean isEventStream(HttpResponse<InputStream> response) {
        return response.headers().firstValue("Content-Type")
                .map(value -> value.contains("text/event-stream"))
                .orElse(false);
    }

    private Object consumeEventStream(InputStream inputStream, String id) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        SseParser parser = new SseParser();
        String line;
        while ((line = reader.readLine()) != null) {
            for (SseParser.Event event : parser.feed(line + "\n")) {
                JSONObject message = messageOf(event, id);
                if (message == null) {
                    continue;
                }
                raiseIfError(message);
                return message.get("result");
            }
        }
        for (SseParser.Event event : parser.end()) {
            JSONObject message = messageOf(event, id);
            if (message == null) {
                continue;
            }
            raiseIfError(message);
            return message.get("result");
        }
        throw new JsonRpcClientException("Stream ended without a response");
    }

    private JSONObject messageOf(SseParser.Event event, String id) {
        if (!"message".equals(event.getEvent())) {
            return null;
        }
        JSONObject message = parseMessage(event.getData());
        if (message == null || !id.equals(String.valueOf(message.get("id")))) {
            return null;
        }
        return message;
    }

    private void raiseIfError(JSONObject message) {
        Object error = message.get("error");
        if (error instanceof JSONObject errorObject) {
            int code = errorObject.getIntValue("code", ErrorEnum.INTERNAL_ERROR.getCode());
            throw ErrorEnum.getException(code, errorObject.getString("message"));
        }
    }

    private JSONObject parseMessage(String data) {
        try {
            Object parsed = JSONUtils.parse(data);
            return parsed instanceof JSONObject ? (JSONObject) parsed : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String requestBody(String id, String method, Object[] args) {
        return JSONUtils.toString(new RequestDto(id, RequestUtils.JSONRPC, resolveMethod(method), args));
    }

    private String resolveMethod(String method) {
        if (method.indexOf('/') >= 0 || method.indexOf('.') >= 0) {
            return method;
        }
        return name + "/" + method;
    }

    private String endpoint() {
        String address = url != null ? url.value() : discovery != null ? discovery.value().get(name) : null;
        if (address == null || address.isEmpty()) {
            throw new JsonRpcClientException("Without invalid address.");
        }
        String first = address.split(",")[0].trim();
        if (first.indexOf("://") < 0) {
            first = "http://" + first;
        }
        URI uri = URI.create(first);
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = DEFAULT_PATH;
        }
        return uri.getScheme() + "://" + uri.getRawAuthority() + path;
    }

    private class StreamIterator implements Iterator<Object>, AutoCloseable {

        private final String id;

        private final String body;

        private final Deque<Object> values = new ArrayDeque<>();

        private HttpResponse<InputStream> response;

        private BufferedReader reader;

        private SseParser parser;

        private boolean unary;

        private boolean finished;

        private JsonRpcException failure;

        private StreamIterator(String id, String body) {
            this.id = id;
            this.body = body;
        }

        @Override
        public boolean hasNext() {
            if (values.isEmpty() && !finished && failure == null) {
                advance();
            }
            if (failure != null) {
                throw failure;
            }
            return !values.isEmpty();
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new java.util.NoSuchElementException();
            }
            return values.pollFirst();
        }

        private void advance() {
            try {
                if (response == null) {
                    response = send(body);
                    if (isEventStream(response)) {
                        reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
                        parser = new SseParser();
                    } else {
                        unary = true;
                    }
                }
                if (unary) {
                    readUnary();
                    return;
                }
                readEvents();
            } catch (JsonRpcException e) {
                failure = e;
            } catch (IOException e) {
                failure = new JsonRpcClientException(e.getMessage());
            }
        }

        private void readUnary() throws IOException {
            String data = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            finished = true;
            JSONObject message = parseMessage(data);
            if (message == null) {
                throw new JsonRpcClientException("Invalid response");
            }
            raiseIfError(message);
            values.addLast(message.get("result"));
        }

        private void readEvents() throws IOException {
            String line;
            while ((line = reader.readLine()) != null) {
                if (dispatch(parser.feed(line + "\n"))) {
                    return;
                }
            }
            finished = true;
            dispatch(parser.end());
        }

        private boolean dispatch(Iterable<SseParser.Event> events) {
            for (SseParser.Event event : events) {
                JSONObject message = messageOf(event, id);
                if (message == null) {
                    continue;
                }
                raiseIfError(message);
                values.addLast(message.get("result"));
                return true;
            }
            return false;
        }

        @Override
        public void close() {
            finished = true;
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.debug("Failed to close the streamable http stream.", e);
                }
            } else if (response != null) {
                try {
                    response.body().close();
                } catch (IOException e) {
                    log.debug("Failed to close the streamable http stream.", e);
                }
            }
        }
    }
}
