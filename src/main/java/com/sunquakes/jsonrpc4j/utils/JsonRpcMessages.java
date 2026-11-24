package com.sunquakes.jsonrpc4j.utils;

import com.sunquakes.jsonrpc4j.ErrorEnum;

/**
 * Builds JSON-RPC 2.0 response and notification payloads that always keep the
 * {@code id} and {@code result} members, including explicit {@code null}
 * values.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
public class JsonRpcMessages {

    private JsonRpcMessages() {
    }

    public static String result(Object id, Object result) {
        return "{\"id\":" + value(id) + ",\"jsonrpc\":\"2.0\",\"result\":" + value(result) + "}";
    }

    public static String error(Object id, int code) {
        return error(id, code, null);
    }

    public static String error(Object id, int code, String data) {
        StringBuilder message = new StringBuilder();
        message.append("{\"id\":").append(value(id))
                .append(",\"jsonrpc\":\"2.0\",\"error\":{\"code\":").append(code)
                .append(",\"message\":").append(JSONUtils.toString(ErrorEnum.getTextByCode(code)));
        if (data != null) {
            message.append(",\"data\":").append(JSONUtils.toString(data));
        }
        message.append("}}");
        return message.toString();
    }

    public static String progress(Object token, int progress, Object message) {
        return "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/progress\",\"params\":{\"progressToken\":"
                + value(token) + ",\"progress\":" + progress + ",\"message\":" + value(message) + "}}";
    }

    private static String value(Object value) {
        return value == null ? "null" : JSONUtils.toString(value);
    }
}
