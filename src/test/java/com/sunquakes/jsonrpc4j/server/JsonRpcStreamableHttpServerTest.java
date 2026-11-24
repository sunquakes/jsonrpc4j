package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.client.JsonRpcStreamableHttpClient;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-streamable-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcStreamableHttpServerTest extends StreamableHttpTestSupport {

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
    void testInitializeWithoutParams() throws Exception {
        HttpResponse<String> response = post(null, null, "{\"jsonrpc\":\"2.0\",\"id\":\"init\",\"method\":\"initialize\"}");
        assertEquals(200, response.statusCode());
        assertEquals("2025-06-18", JSON.parseObject(response.body()).getJSONObject("result").getString("protocolVersion"));
    }

    @Test
    void testProtocolVersionNegotiation() throws Exception {
        HttpResponse<String> response = post(null, "2025-03-26", "{\"jsonrpc\":\"2.0\",\"id\":\"v\",\"method\":\"ping\"}");
        assertEquals(200, response.statusCode());
        assertEquals("2025-03-26", response.headers().firstValue("Mcp-Protocol-Version").orElse(null));
        assertEquals("{}", JSON.parseObject(response.body()).getJSONObject("result").toString());
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
    void testBatchLegacyRequests() throws Exception {
        HttpResponse<String> response = post(null, null,
                "[{\"id\":\"1\",\"jsonrpc\":\"2.0\",\"method\":\"JsonRpc/add\",\"params\":{\"a\":1,\"b\":2}}," +
                        "{\"id\":\"2\",\"jsonrpc\":\"2.0\",\"method\":\"JsonRpc/add\",\"params\":{\"a\":3,\"b\":4}}]");
        assertEquals(200, response.statusCode());
        assertEquals(2, JSON.parseArray(response.body()).size());
        assertEquals(3, JSON.parseArray(response.body()).getJSONObject(0).get("result"));
        assertEquals(7, JSON.parseArray(response.body()).getJSONObject(1).get("result"));
    }

    @Test
    void testPrimitiveJsonRejected() throws Exception {
        HttpResponse<String> response = post(null, null, "123");
        assertEquals(400, response.statusCode());
        assertEquals(-32600, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testMissingMethod() throws Exception {
        HttpResponse<String> response = post(null, null, "{\"jsonrpc\":\"2.0\",\"id\":\"missing\"}");
        assertEquals(200, response.statusCode());
        assertEquals(-32600, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testNotificationAccepted() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, response.statusCode());
        assertEquals("", response.body());
    }

    @Test
    void testMcpNotificationWithIdAccepted() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"n\",\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, response.statusCode());
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
    void testStreamingFailure() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"broken\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/broken\",\"params\":{}}");
        assertEquals(200, response.statusCode());
        List<JSONObject> messages = sseMessages(response.body());
        assertEquals(-32603, messages.get(messages.size() - 1).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testLegacyInvocationFailure() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"fail\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/fail\",\"params\":{}}");
        assertEquals(200, response.statusCode());
        assertEquals(-32601, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testLegacyInvalidParams() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"params\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/range\",\"params\":{}}");
        assertEquals(200, response.statusCode());
        assertEquals(-32603, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testLegacyIllFormedMethod() throws Exception {
        HttpResponse<String> response = post(null, null,
                "{\"id\":\"method\",\"jsonrpc\":\"2.0\",\"method\":\"JsonRpc/a/b\",\"params\":{}}");
        assertEquals(200, response.statusCode());
        assertEquals(-32601, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testUnknownSession() throws Exception {
        HttpResponse<String> response = post("missing-session", null, INITIALIZE);
        assertEquals(404, response.statusCode());
    }

    @Test
    void testNotAcceptableWithoutEventStream() throws Exception {
        HttpRequest request = request(ENDPOINT)
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
        HttpRequest request = request(ENDPOINT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("Origin", "http://evil.example")
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(403, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testMalformedOriginRejected() throws Exception {
        HttpRequest request = request(ENDPOINT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("Origin", "http://[")
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(403, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testUnsupportedMethod() throws Exception {
        HttpRequest request = request(ENDPOINT)
                .header("Accept", "application/json, text/event-stream")
                .PUT(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(405, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testGetChannelReplayAndDelete() throws Exception {
        HttpResponse<String> initialize = post(null, null, INITIALIZE);
        String sessionId = initialize.headers().firstValue("Mcp-Session-Id").orElseThrow();

        HttpResponse<String> stream = post(sessionId, null,
                "{\"id\":\"replay\",\"jsonrpc\":\"2.0\",\"method\":\"Streamable/range\",\"params\":{\"count\":2}}");
        List<String> eventIds = sseEventIds(stream.body());
        assertEquals(2, eventIds.size());

        HttpRequest replay = request(ENDPOINT)
                .header("Accept", "text/event-stream")
                .header("Mcp-Session-Id", sessionId)
                .header("Last-Event-ID", eventIds.get(0))
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(replay, HttpResponse.BodyHandlers.ofInputStream());
        assertEquals(200, response.statusCode());
        String replayed = readFrames(response.body(), 1);
        assertTrue(replayed.contains("\"result\":1"));

        HttpRequest delete = request(ENDPOINT)
                .header("Mcp-Session-Id", sessionId)
                .DELETE()
                .build();
        assertEquals(204, httpClient.send(delete, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testGetChannelValidation() throws Exception {
        assertEquals(400, get(null, null).statusCode());
        assertEquals(404, get("missing-session", null).statusCode());
    }

    @Test
    void testGetNotAcceptable() throws Exception {
        HttpRequest request = request(ENDPOINT)
                .header("Accept", "application/json")
                .GET()
                .build();
        assertEquals(406, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testReplayUnknownEventId() throws Exception {
        String sessionId = post(null, null, INITIALIZE).headers().firstValue("Mcp-Session-Id").orElseThrow();
        assertEquals(400, get(sessionId, "missing-event").statusCode());
    }

    @Test
    void testDeleteValidation() throws Exception {
        HttpRequest missing = request(ENDPOINT).DELETE().build();
        assertEquals(400, httpClient.send(missing, HttpResponse.BodyHandlers.ofString()).statusCode());

        HttpRequest unknown = request(ENDPOINT).header("Mcp-Session-Id", "missing-session").DELETE().build();
        assertEquals(404, httpClient.send(unknown, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testUnknownPath() throws Exception {
        HttpRequest request = request("http://127.0.0.1:3300/unknown")
                .POST(HttpRequest.BodyPublishers.ofString(INITIALIZE))
                .build();
        assertEquals(404, httpClient.send(request, HttpResponse.BodyHandlers.ofString()).statusCode());
    }

    @Test
    void testClientCallAndStream() {
        try (JsonRpcStreamableHttpClient client = client("127.0.0.1:3300")) {
            assertEquals("hello", client.call("echo", "hello"));
            assertEquals(0, client.call("range", 3));
            assertEquals(List.of(0, 1, 2), client.stream("numbers", 3).toList());
            assertEquals("hi", client.handle("Streamable/echo", new Object[]{"hi"}));
        }
    }

    @Test
    void testClientError() {
        try (JsonRpcStreamableHttpClient client = client("127.0.0.1:3300")) {
            assertThrows(JsonRpcException.class, () -> client.call("fail"));
        }
    }

    @Test
    void testClientUnknownEndpoint() {
        try (JsonRpcStreamableHttpClient client = client("127.0.0.1:3300/unknown")) {
            assertThrows(JsonRpcException.class, () -> client.call("echo", "hello"));
        }
    }

    @Test
    void testClientCloseWithoutSession() {
        JsonRpcStreamableHttpClient client = client("127.0.0.1:3300");
        client.close();
        client.close();
    }

    private JsonRpcStreamableHttpClient client(String url) {
        Config<Object> config = new Config<>();
        config.put(new ConfigEntry<>("protocol", "streamable-http"));
        config.put(new ConfigEntry<>("name", "Streamable"));
        config.put(new ConfigEntry<>("url", url));
        return new JsonRpcStreamableHttpClient(config);
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
