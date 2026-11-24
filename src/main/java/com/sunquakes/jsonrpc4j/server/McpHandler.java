package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.JsonRpcMessages;
import com.sunquakes.jsonrpc4j.utils.StreamableUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * Publishes the registered JSON-RPC services as MCP tools and implements the
 * minimal MCP lifecycle methods used by agent clients.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
@Slf4j
public class McpHandler {

    public static final List<String> PROTOCOL_VERSIONS = List.of("2025-06-18", "2025-03-26");

    public static final String DEFAULT_PROTOCOL_VERSION = PROTOCOL_VERSIONS.get(0);

    private static final Set<String> METHODS = Set.of("initialize", "ping", "tools/list", "tools/call");

    private static final String SERVER_NAME = "jsonrpc4j";

    private static final String SERVER_VERSION = "3.0.0";

    private final Map<String, Tool> tools = new LinkedHashMap<>();

    private final Map<String, List<String>> serviceTools = new HashMap<>();

    public static boolean handles(String method) {
        return METHODS.contains(method)
                || method.startsWith("notifications/")
                || method.indexOf('/') < 0;
    }

    public synchronized void addService(String serviceName, Object service) {
        List<String> previous = serviceTools.remove(serviceName);
        if (previous != null) {
            previous.forEach(tools::remove);
        }
        List<String> names = new ArrayList<>();
        for (Method method : service.getClass().getMethods()) {
            if (method.getDeclaringClass() == Object.class || method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }
            String baseName = serviceName + "_" + method.getName();
            String toolName = baseName;
            int suffix = 2;
            while (tools.containsKey(toolName)) {
                toolName = baseName + "_" + suffix++;
            }
            Parameter[] parameters = method.getParameters();
            String[] parameterNames = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                parameterNames[i] = parameters[i].getName();
            }
            tools.put(toolName, new Tool(toolName, serviceName + "/" + method.getName(), service, method, parameterNames));
            names.add(toolName);
        }
        serviceTools.put(serviceName, names);
    }

    public Outcome handle(Object id, String method, Object params) {
        switch (method) {
            case "initialize":
                return Outcome.json(JsonRpcMessages.result(id, initialize(params)));
            case "ping":
                return Outcome.json(JsonRpcMessages.result(id, new JSONObject()));
            case "tools/list":
                return Outcome.json(JsonRpcMessages.result(id, listTools()));
            case "tools/call":
                return callTool(id, params);
            default:
                if (method.startsWith("notifications/")) {
                    return Outcome.accepted();
                }
                return Outcome.json(JsonRpcMessages.error(id, ErrorEnum.METHOD_NOT_FOUND.getCode()));
        }
    }

    private JSONObject initialize(Object params) {
        String requested = JSONUtils.isObject(params) ? stringValue(JSONUtils.get(params, "protocolVersion")) : null;
        String protocolVersion = PROTOCOL_VERSIONS.contains(requested) ? requested : DEFAULT_PROTOCOL_VERSION;
        JSONObject result = new JSONObject();
        result.put("protocolVersion", protocolVersion);
        JSONObject capabilities = new JSONObject();
        capabilities.put("tools", new JSONObject());
        result.put("capabilities", capabilities);
        JSONObject serverInfo = new JSONObject();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);
        result.put("serverInfo", serverInfo);
        return result;
    }

    private JSONObject listTools() {
        JSONArray list = new JSONArray();
        for (Tool tool : tools.values()) {
            JSONObject item = new JSONObject();
            item.put("name", tool.name);
            item.put("description", tool.description);
            JSONObject schema = new JSONObject();
            schema.put("type", "object");
            JSONObject properties = new JSONObject();
            JSONArray required = new JSONArray();
            for (String parameterName : tool.parameterNames) {
                properties.put(parameterName, new JSONObject());
                required.add(parameterName);
            }
            schema.put("properties", properties);
            schema.put("required", required);
            item.put("inputSchema", schema);
            list.add(item);
        }
        JSONObject result = new JSONObject();
        result.put("tools", list);
        return result;
    }

    private Outcome callTool(Object id, Object params) {
        if (!JSONUtils.isObject(params)) {
            return Outcome.json(JsonRpcMessages.error(id, ErrorEnum.INVALID_PARAMS.getCode()));
        }
        String name = stringValue(JSONUtils.get(params, "name"));
        Tool tool = name == null ? null : tools.get(name);
        if (tool == null) {
            return Outcome.json(JsonRpcMessages.error(id, ErrorEnum.INVALID_PARAMS.getCode()));
        }
        Object[] arguments;
        try {
            arguments = tool.arguments(JSONUtils.get(params, "arguments"));
        } catch (Exception e) {
            return Outcome.json(JsonRpcMessages.error(id, ErrorEnum.INVALID_PARAMS.getCode()));
        }
        Object meta = JSONUtils.get(params, "_meta");
        Object progressToken = JSONUtils.isObject(meta) ? JSONUtils.get(meta, "progressToken") : null;
        try {
            Object value = tool.method.invoke(tool.service, arguments);
            if (value instanceof CompletionStage<?> stage) {
                value = stage.toCompletableFuture().join();
            }
            if (StreamableUtils.isStreamable(value)) {
                Iterator<Object> iterator = StreamableUtils.toIterator(value);
                if (progressToken == null) {
                    return Outcome.json(JsonRpcMessages.result(id, aggregate(iterator)));
                }
                return Outcome.stream(progressStream(id, progressToken, iterator));
            }
            List<Object> values = new ArrayList<>();
            values.add(value);
            return Outcome.json(JsonRpcMessages.result(id, toolResult(values, false)));
        } catch (Exception e) {
            return Outcome.json(JsonRpcMessages.result(id, toolResult(List.of(errorText(e)), true)));
        }
    }

    private Object aggregate(Iterator<Object> iterator) {
        List<Object> values = new ArrayList<>();
        try {
            while (iterator.hasNext()) {
                values.add(iterator.next());
            }
        } catch (Exception e) {
            return toolResult(List.of(errorText(e)), true);
        }
        return toolResult(values, false);
    }

    private Iterator<String> progressStream(Object id, Object progressToken, Iterator<Object> source) {
        return new Iterator<>() {

            private final List<Object> values = new ArrayList<>();

            private int progress;

            private String next;

            private boolean done;

            @Override
            public boolean hasNext() {
                if (next == null && !done) {
                    advance();
                }
                return next != null;
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                String value = next;
                next = null;
                return value;
            }

            private void advance() {
                try {
                    if (source.hasNext()) {
                        Object value = source.next();
                        values.add(value);
                        progress++;
                        next = JsonRpcMessages.progress(progressToken, progress, textContent(value));
                    } else {
                        done = true;
                        next = JsonRpcMessages.result(id, toolResult(values, false));
                    }
                } catch (Exception e) {
                    done = true;
                    next = JsonRpcMessages.result(id, toolResult(List.of(errorText(e)), true));
                }
            }
        };
    }

    private JSONObject toolResult(List<?> values, boolean isError) {
        JSONObject result = new JSONObject();
        JSONArray content = new JSONArray();
        for (Object value : values) {
            content.add(textContent(value));
        }
        result.put("content", content);
        result.put("isError", isError);
        return result;
    }

    private JSONObject textContent(Object value) {
        JSONObject content = new JSONObject();
        content.put("type", "text");
        if (value == null) {
            content.put("text", "");
        } else if (value instanceof String string) {
            content.put("text", string);
        } else {
            content.put("text", JSONUtils.toString(value));
        }
        return content;
    }

    private String errorText(Throwable throwable) {
        Throwable cause = throwable instanceof java.lang.reflect.InvocationTargetException && throwable.getCause() != null
                ? throwable.getCause()
                : throwable;
        if (cause == null) {
            return "";
        }
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private static class Tool {

        private final String name;

        private final String description;

        private final Object service;

        private final Method method;

        private final String[] parameterNames;

        private Tool(String name, String description, Object service, Method method, String[] parameterNames) {
            this.name = name;
            this.description = description;
            this.service = service;
            this.method = method;
            this.parameterNames = Objects.requireNonNull(parameterNames);
        }

        private Object[] arguments(Object arguments) {
            Object[] values = new Object[parameterNames.length];
            if (arguments == null) {
                return values;
            }
            if (JSONUtils.isArray(arguments)) {
                Object[] source = JSONUtils.toArray(arguments);
                System.arraycopy(source, 0, values, 0, Math.min(source.length, values.length));
                return values;
            }
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0; i < parameterNames.length; i++) {
                values[i] = JSONUtils.toJavaObject(types[i], JSONUtils.get(arguments, parameterNames[i]));
            }
            return values;
        }
    }

    public static class Outcome {

        private final boolean accepted;

        private final String json;

        private final Iterator<String> stream;

        private Outcome(boolean accepted, String json, Iterator<String> stream) {
            this.accepted = accepted;
            this.json = json;
            this.stream = stream;
        }

        public static Outcome accepted() {
            return new Outcome(true, null, null);
        }

        public static Outcome json(String json) {
            return new Outcome(false, json, null);
        }

        public static Outcome stream(Iterator<String> stream) {
            return new Outcome(false, null, stream);
        }

        public boolean isAccepted() {
            return accepted;
        }

        public boolean isStream() {
            return stream != null;
        }

        public String getJson() {
            return json;
        }

        public Iterator<String> getStream() {
            return stream;
        }
    }
}
