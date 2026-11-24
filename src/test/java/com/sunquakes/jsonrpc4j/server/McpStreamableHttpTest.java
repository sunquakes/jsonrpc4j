package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-streamable-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class McpStreamableHttpTest {

    private static final String ENDPOINT = "http://127.0.0.1:3300/mcp";

    private static final String INITIALIZE = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\"," +
            "\"params\":{\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\",\"version\":\"1.0\"}}}";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void testToolsList() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"list\",\"method\":\"tools/list\"}");
        assertEquals(200, response.statusCode());
        JSONArray tools = JSON.parseObject(response.body()).getJSONObject("result").getJSONArray("tools");
        JSONObject echo = null;
        for (int i = 0; i < tools.size(); i++) {
            JSONObject tool = tools.getJSONObject(i);
            if ("Streamable_echo".equals(tool.getString("name"))) {
                echo = tool;
            }
        }
        assertTrue(echo != null);
        assertEquals("Streamable/echo", echo.getString("description"));
        assertEquals("object", echo.getJSONObject("inputSchema").getString("type"));
        assertTrue(echo.getJSONObject("inputSchema").getJSONObject("properties").containsKey("value"));
        assertTrue(echo.getJSONObject("inputSchema").getJSONArray("required").contains("value"));
    }

    @Test
    void testToolsCallUnary() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_echo", "{\"value\":\"hi\"}");
        assertFalse(result.getBooleanValue("isError"));
        assertEquals("hi", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallAggregatesStream() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_range", "{\"count\":3}");
        JSONArray content = result.getJSONArray("content");
        assertEquals(3, content.size());
        for (int i = 0; i < content.size(); i++) {
            assertEquals(String.valueOf(i), content.getJSONObject(i).getString("text"));
        }
    }

    @Test
    void testToolsCallProgressStream() throws Exception {
        String sessionId = initialize();
        String body = "{\"jsonrpc\":\"2.0\",\"id\":\"progress\",\"method\":\"tools/call\",\"params\":{" +
                "\"name\":\"Streamable_range\",\"arguments\":{\"count\":3},\"_meta\":{\"progressToken\":\"tok\"}}}";
        HttpResponse<String> response = post(sessionId, body);
        assertEquals(200, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("text/event-stream"));

        List<JSONObject> messages = sseMessages(response.body());
        assertEquals(4, messages.size());
        for (int i = 0; i < 3; i++) {
            JSONObject notification = messages.get(i);
            assertEquals("notifications/progress", notification.getString("method"));
            JSONObject params = notification.getJSONObject("params");
            assertEquals("tok", params.getString("progressToken"));
            assertEquals(i + 1, params.getIntValue("progress"));
        }
        JSONObject result = messages.get(3);
        assertEquals("progress", result.getString("id"));
        assertEquals(3, result.getJSONObject("result").getJSONArray("content").size());
    }

    @Test
    void testToolsCallError() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_fail", "{}");
        assertTrue(result.getBooleanValue("isError"));
        assertEquals("boom", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallUnknownTool() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"unknown\",\"method\":\"tools/call\"," +
                        "\"params\":{\"name\":\"missing\",\"arguments\":{}}}");
        assertEquals(200, response.statusCode());
        JSONObject message = JSON.parseObject(response.body());
        assertEquals(-32602, message.getJSONObject("error").getIntValue("code"));
        assertNotEquals(null, message.get("id"));
    }

    @Test
    void testLegacyProtocolStillWorks() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"id\":\"legacy\",\"jsonrpc\":\"2.0\",\"method\":\"JsonRpc/add\",\"params\":{\"a\":2,\"b\":5}}");
        assertEquals(200, response.statusCode());
        assertEquals(7, JSON.parseObject(response.body()).get("result"));
    }

    private String initialize() throws Exception {
        return post(null, INITIALIZE).headers().firstValue("Mcp-Session-Id").orElseThrow();
    }

    private JSONObject callTool(String sessionId, String name, String arguments) throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":\"call\",\"method\":\"tools/call\",\"params\":{" +
                "\"name\":\"" + name + "\",\"arguments\":" + arguments + "}}";
        HttpResponse<String> response = post(sessionId, body);
        assertEquals(200, response.statusCode());
        return JSON.parseObject(response.body()).getJSONObject("result");
    }

    private HttpResponse<String> post(String sessionId, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        if (sessionId != null) {
            builder.header("Mcp-Session-Id", sessionId);
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
}
