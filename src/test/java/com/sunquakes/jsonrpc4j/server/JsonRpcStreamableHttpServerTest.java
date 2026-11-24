package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.client.JsonRpcStreamableHttpClient;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-streamable-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcStreamableHttpServerTest {

    private static final String ENDPOINT = "http://127.0.0.1:3300/mcp";

    private static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"," +
            "\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void testInitializeHandshake() throws Exception {
        HttpResponse<String> response = post(null, null, INITIALIZE);
        assertEquals(200, response.statusCode());
        String sessionId = response.headers().firstValue("Mcp-Session-Id").orElse(null);
        assertNotNull(sessionId);
        assertEquals("2025-06-18", response.headers().firstValue("Mcp-Protocol-Version").orElse(null));

        JSONObject message = JSON.parseObject(response.body());
        assertEquals(1, message.get("id"));
        JSONObject result = message.getJSONObject("result");
        assertEquals("2025-06-18", result.getString("protocolVersion"));
        assertEquals("jsonrpc4j", result.getJSONObject("serverInfo").getString("name"));
        assertNotNull(result.getJSONObject("capabilities").get("tools"));
    }

    @Test
    void testProtocolVersionNegotiation() throws Exception {
        HttpResponse<String> response = post(null, "2025-03-26", "{\"jsonrpc\":\"2.0\",\"id\":\"v\",\"method\":\"ping\"}");
        assertEquals(200, response.statusCode());
        assertEquals("2025-03-26", response.headers().firstValue("Mcp-Protocol-Version").orElse(null));
        assertEquals(new JSONObject().toString(), JSON.parseObject(response.body()).getJSONObject("result").toString());
    }

    @Test
    void testUnsupportedProtocolVersion() throws Exception {
        HttpResponse<String> response = post(null, "1900-01-01", INITIALIZE);
        assertEquals(400, response.statusCode());
        assertEquals("2025-06-18", response.headers().firstValue("Mcp-Protocol-Version").orElse(null));
    }

    @Test
    void testLegacyDispatch() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"1\",\"jsonrpc\":\"2.0\",\"method\":\"JsonRpc/add\",\"params\":{\"a\":1,\"b\":2}}");
        assertEquals(200, response.statusCode());
        assertEquals("application/json", response.headers().firstValue("Content-Type").orElse(null));
        JSONObject message = JSON.parseObject(response.body());
        assertEquals("1", message.get("id"));
        assertEquals(3, message.get("result"));
    }

    @Test
    void testNotificationAccepted() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, response.statusCode());
        assertEquals("", response.body());
    }

    @Test
    void testStreamingArrayOverSse() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"stream\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/range\",\"params\":{\"count\":3}}");
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/event-stream"));
        List<JSONObject> messages = sseMessages(response.body());
        assertEquals(3, messages.size());
        for (int i = 0; i < messages.size(); i++) {
            assertEquals(i, messages.get(i).get("result"));
        }
    }

    @Test
    void testStreamingIterableAndStream() throws Exception {
        HttpResponse<String> iterable = post(null, null,
                "{\"id\":\"iterable\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/sequence\",\"params\":{\"count\":2}}");
        assertEquals(2, sseMessages(iterable.body()).size());

        HttpResponse<String> stream = post(null, null,
                "{\"id\":\"stream\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/numbers\",\"params\":{\"count\":4}}");
        List<JSONObject> messages = sseMessages(stream.body());
        assertEquals(4, messages.size());
        assertEquals(3, messages.get(3).get("result"));
    }

    @Test
    void testUnknownSession() throws Exception {
        HttpResponse<String> response = post("missing-session", null, INITIALIZE);
        assertEquals(404, response.statusCode());
    }

    @Test
    void testNotAcceptableWithoutEventStream() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(406, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testBadJson() throws Exception {
        HttpResponse<String> response = post(null, null, "{");
        assertEquals(400, response.statusCode());
        assertEquals(-32700, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testOriginRejected() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("Origin", "http://evil.example")
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(403, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testGetChannelReplayAndDelete() throws Exception {
        HttpResponse<String> initialize = post(null, null, INITIALIZE);
        String sessionId = initialize.headers().firstValue("Mcp-Session-Id").orElseThrow();

        HttpResponse<String> stream = post(sessionId, null,
                "{\"id\":\"replay\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/range\",\"params\":{\"count\":2}}");
        List<String> eventIds = sseEventIds(stream.body());
        assertEquals(2, eventIds.size());

        HttpRequest replay = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Accept", "text/event-stream")
                .header("Mcp-Session-Id", sessionId)
                .header("Last-Event-ID", eventIds.get(0))
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(replay, HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, response.statusCode());
        String replayed = readFrames(response.body(), 1);
        assertTrue(replayed.contains("\"result\":1"));

        HttpRequest delete = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Mcp-Session-Id", sessionId)
                .DELETE()
                .build();
        assertEquals(204, httpClient.send(delete, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testGetChannelValidation() throws Exception {
        HttpResponse<String> missing = get(null, null);
        assertEquals(400, missing.statusCode());

        HttpResponse<String> unknown = get("missing-session", null);
        assertEquals(404, unknown.statusCode());
    }

    @Test
    void testUnknownPath() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:3300/unknown"))
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(404, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testClientCallAndStream() {
        Config<Object> config = new Config<>();
        config.put(new ConfigEntry<>("protocol", "streamable-http"));
        config.put(new ConfigEntry<>("name", "Streamable"));
        config.put(new ConfigEntry<>("url", "127.0.0.1:3300"));
        try (JsonRpcStreamableHttpClient client = new JsonRpcStreamableHttpClient(config)) {
            assertEquals("hello", client.call("echo", "hello"));
            assertEquals(List.of(0, 1, 2), client.stream("numbers", 3).toList());
        }
    }

    private HttpResponse<String> post(String sessionId, String protocolVersion, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
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

    private HttpResponse<String> get(String sessionId, String lastEventId) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Accept", "text/event-stream")
                .GET();
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
        }
        if (lastEventId != null) {
            builder.header("Last-Event-ID", lastEventId);
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private List<JSONObject> sseMessages(String body) {
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

    private List<String> sseEventIds(String body) {
        List<String> ids = new ArrayList<>();
        for (String line : body.split("\n")) {
            if (line.startsWith("id: ")) {
                ids.add(line.substring("id: ".length()));
            }
        }
        return ids;
    }

    private String readFrames(InputStream inputStream, int frames) throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Future<String> future = executorService.submit(() -> {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            int count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
                if (line.isEmpty() && ++count >= frames) {
                    break;
                }
            }
            return builder.toString();
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } finally {
            inputStream.close();
            executorService.shutdownNow();
        }
    }
}
