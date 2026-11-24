package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

abstract class StreamableHttpTestSupport {

    protected static final String ENDPOINT = "http://127.0.0.1:3300/mcp";

    protected static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"," +
            "\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}";

    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    protected HttpResponse<String> post(String sessionId, String protocolVersion, String body) throws Exception {
        HttpRequest.Builder builder = request(ENDPOINT).header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
        }
        if (protocolVersion != null) {
            builder.header("Mcp-Protocol-Version", protocolVersion);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    protected HttpResponse<String> post(String sessionId, String body) throws Exception {
        return post(sessionId, null, body);
    }

    protected HttpResponse<String> get(String sessionId, String lastEventId) throws Exception {
        HttpRequest.Builder builder = request(ENDPOINT).header("Accept", "text/event-stream").GET();
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
        }
        if (lastEventId != null) {
            builder.header("Last-Event-ID", lastEventId);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    protected HttpRequest.Builder request(String url) {
        return HttpRequest.newBuilder(URI.create(url));
    }

    protected List<JSONObject> sseMessages(String body) {
        List<JSONObject> messages = new ArrayList<>();
        for (String frame : body.split("\n\n")) {
            StringBuilder data = new StringBuilder();
            for (String line : frame.split("\n")) {
                if (line.startsWith("data: ")) {
                    data.append(line.substring("data: ".length()));
                }
            }
            if (!data.isEmpty()) {
                messages.add(JSON.parseObject(data.toString()));
            }
        }
        return messages;
    }

    protected List<String> sseEventIds(String body) {
        List<String> ids = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (line.startsWith("id: ")) {
                ids.add(line.substring("id: ".length()));
            }
        }
        return ids;
    }
}
