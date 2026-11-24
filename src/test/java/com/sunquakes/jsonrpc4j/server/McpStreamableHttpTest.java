package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-streamable-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class McpStreamableHttpTest extends StreamableHttpTestSupport {

    @Test
    void testInitializeResult() throws Exception {
        JSONObject message = JSON.parseObject(post(null, INITIALIZE).body());
        assertEquals(1, message.get("id"));
        JSONObject result = message.getJSONObject("result");
        assertEquals("2025-06-18", result.getString("protocolVersion"));
        JSONObject serverInfo = result.getJSONObject("serverInfo");
        assertEquals("jsonrpc4j", serverInfo.getString("name"));
        assertNotNull(serverInfo.getString("version"));
        assertNotNull(result.getJSONObject("capabilities").get("tools"));
    }

    @Test
    void testInitializeNegotiatesSupportedVersion() throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"initialize\"," +
                "\"params\":{\"protocolVersion\":\"2025-03-26\",\"capabilities\":{},\"clientInfo\":{\"name\":\"test\"}}}";
        JSONObject result = JSON.parseObject(post(null, body).body()).getJSONObject("result");
        assertEquals("2025-03-26", result.getString("protocolVersion"));
    }

    @Test
    void testInitializeFallsBackToDefaultVersion() throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"initialize\"," +
                "\"params\":{\"protocolVersion\":\"1900-01-01\"}}";
        JSONObject result = JSON.parseObject(post(null, body).body()).getJSONObject("result");
        assertEquals("2025-06-18", result.getString("protocolVersion"));
    }

    @Test
    void testPing() throws Exception {
        String sessionId = initialize();
        JSONObject message = JSON.parseObject(post(sessionId, "{\"jsonrpc\":\"2.0\",\"id\":\"ping\",\"method\":\"ping\"}").body());
        assertEquals("ping", message.get("id"));
        assertTrue(message.getJSONObject("result").isEmpty());
    }

    @Test
    void testNotificationAccepted() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, response.statusCode());
        assertEquals("", response.body());
    }

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
        assertNotNull(echo);
        assertEquals("Streamable/echo", echo.getString("description"));
        JSONObject schema = echo.getJSONObject("inputSchema");
        assertEquals("object", schema.getString("type"));
        assertTrue(schema.getJSONObject("properties").containsKey("value"));
        assertTrue(schema.getJSONArray("required").contains("value"));
    }

    @Test
    void testToolsCallUnary() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_echo", "{\"value\":\"hi\"}");
        assertFalse(result.getBooleanValue("isError"));
        assertEquals("hi", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallKeepsNumericId() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId, "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"tools/call\"," +
                "\"params\":{\"name\":\"Streamable_echo\",\"arguments\":{\"value\":\"hi\"}}}");
        assertEquals(7, JSON.parseObject(response.body()).get("id"));
    }

    @Test
    void testToolsCallMissingArguments() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_echo", null);
        assertEquals("", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallArrayArguments() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_echo", "[\"hi\"]");
        assertEquals("hi", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallAggregatesArray() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_range", "{\"count\":3}");
        JSONArray content = result.getJSONArray("content");
        assertEquals(3, content.size());
        for (int i = 0; i < content.size(); i++) {
            assertEquals(String.valueOf(i), content.getJSONObject(i).getString("text"));
        }
    }

    @Test
    void testToolsCallAggregatesIterableAndStream() throws Exception {
        String sessionId = initialize();
        assertEquals(2, callTool(sessionId, "Streamable_sequence", "{\"count\":2}")
                .getJSONArray("content").size());
        assertEquals(4, callTool(sessionId, "Streamable_numbers", "{\"count\":4}")
                .getJSONArray("content").size());
    }

    @Test
    void testToolsCallCompletionStage() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_later", null);
        assertEquals("later", result.getJSONArray("content").getJSONObject(0).getString("text"));
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
        JSONObject result = callTool(sessionId, "Streamable_fail", null);
        assertTrue(result.getBooleanValue("isError"));
        assertEquals("boom", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallBrokenStreamAggregatesError() throws Exception {
        String sessionId = initialize();
        JSONObject result = callTool(sessionId, "Streamable_broken", null);
        assertTrue(result.getBooleanValue("isError"));
        assertEquals("broken", result.getJSONArray("content").getJSONObject(0).getString("text"));
    }

    @Test
    void testToolsCallBrokenStreamWithProgress() throws Exception {
        String sessionId = initialize();
        String body = "{\"jsonrpc\":\"2.0\",\"id\":\"broken\",\"method\":\"tools/call\",\"params\":{" +
                "\"name\":\"Streamable_broken\",\"_meta\":{\"progressToken\":\"tok\"}}}";
        HttpResponse<String> response = post(sessionId, body);
        assertEquals(200, response.statusCode());
        List<JSONObject> messages = sseMessages(response.body());
        JSONObject result = messages.get(messages.size() - 1);
        assertTrue(result.getJSONObject("result").getBooleanValue("isError"));
        assertEquals("broken", result.getJSONObject("result").getJSONArray("content")
                .getJSONObject(0).getString("text"));
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
        assertEquals("unknown", message.get("id"));
    }

    @Test
    void testToolsCallInvalidParams() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"params\",\"method\":\"tools/call\",\"params\":\"oops\"}");
        assertEquals(200, response.statusCode());
        assertEquals(-32602, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testToolsCallArgumentsNotObject() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"arguments\",\"method\":\"tools/call\"," +
                        "\"params\":{\"name\":\"Streamable_echo\",\"arguments\":\"oops\"}}");
        assertEquals(200, response.statusCode());
        assertEquals(-32602, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
    }

    @Test
    void testUnknownMcpMethod() throws Exception {
        String sessionId = initialize();
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"unknown-method\",\"method\":\"unknown\"}");
        assertEquals(200, response.statusCode());
        assertEquals(-32601, JSON.parseObject(response.body()).getJSONObject("error").getIntValue("code"));
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
        HttpResponse<String> response = post(null, INITIALIZE);
        assertEquals(200, response.statusCode());
        return response.headers().firstValue("Mcp-Session-Id").orElseThrow();
    }

    private JSONObject callTool(String sessionId, String name, String arguments) throws Exception {
        String params = "\"name\":\"" + name + "\"" + (arguments == null ? "" : ",\"arguments\":" + arguments);
        HttpResponse<String> response = post(sessionId,
                "{\"jsonrpc\":\"2.0\",\"id\":\"call\",\"method\":\"tools/call\",\"params\":{" + params + "}}");
        assertEquals(200, response.statusCode());
        return JSON.parseObject(response.body()).getJSONObject("result");
    }
}
