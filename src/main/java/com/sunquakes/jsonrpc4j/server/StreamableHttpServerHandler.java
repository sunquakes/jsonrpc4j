package com.sunquakes.jsonrpc4j.server;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.ErrorEnum;
import com.sunquakes.jsonrpc4j.JsonRpcService;
import com.sunquakes.jsonrpc4j.exception.InvalidParamsException;
import com.sunquakes.jsonrpc4j.exception.MethodNotFoundException;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import com.sunquakes.jsonrpc4j.utils.JsonRpcMessages;
import com.sunquakes.jsonrpc4j.utils.StreamableUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Netty handler implementing the MCP Streamable HTTP transport on a single
 * {@code /mcp} endpoint.
 *
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.1.0
 * @since 3.1.0
 **/
@Slf4j
@ChannelHandler.Sharable
public class StreamableHttpServerHandler extends ChannelInboundHandlerAdapter {

    public static final String PATH = "/mcp";

    public static final String SESSION_HEADER = "Mcp-Session-Id";

    public static final String PROTOCOL_HEADER = "Mcp-Protocol-Version";

    private static final String LAST_EVENT_ID_HEADER = "Last-Event-ID";

    private static final int EVENT_BUFFER_LIMIT = 256;

    private static final long KEEPALIVE_SECONDS = 15;

    private final JsonRpcServerHandler jsonRpcServerHandler;

    private final McpHandler mcpHandler = new McpHandler();

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public StreamableHttpServerHandler(ApplicationContext applicationContext) {
        this.jsonRpcServerHandler = new JsonRpcServerHandler(applicationContext);
        registerServices(applicationContext);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof FullHttpRequest)) {
            ctx.fireChannelRead(msg);
            return;
        }
        FullHttpRequest request = (FullHttpRequest) msg;
        try {
            handle(ctx, request);
        } finally {
            request.release();
        }
    }

    private void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion version = request.protocolVersion();
        String path;
        try {
            path = new QueryStringDecoder(request.uri()).path();
        } catch (Exception e) {
            sendJson(ctx, version, HttpResponseStatus.NOT_FOUND, "", null);
            return;
        }
        if (!PATH.equals(path)) {
            sendJson(ctx, version, HttpResponseStatus.NOT_FOUND, "", null);
            return;
        }
        if (!isOriginAllowed(request)) {
            sendJson(ctx, version, HttpResponseStatus.FORBIDDEN, "", null);
            return;
        }
        HttpMethod method = request.method();
        if (HttpMethod.POST.equals(method)) {
            handlePost(ctx, request);
        } else if (HttpMethod.GET.equals(method)) {
            handleGet(ctx, request);
        } else if (HttpMethod.DELETE.equals(method)) {
            handleDelete(ctx, request);
        } else {
            sendJson(ctx, version, HttpResponseStatus.METHOD_NOT_ALLOWED, "", null);
        }
    }

    private void handlePost(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion version = request.protocolVersion();
        String accept = request.headers().get(HttpHeaderNames.ACCEPT);
        if (accept == null || !accept.contains("application/json") || !accept.contains("text/event-stream")) {
            sendJson(ctx, version, HttpResponseStatus.NOT_ACCEPTABLE, "", null);
            return;
        }
        String protocolVersion = negotiate(request.headers().get(PROTOCOL_HEADER));
        if (protocolVersion == null) {
            Map<String, String> headers = new ConcurrentHashMap<>();
            headers.put(PROTOCOL_HEADER, McpHandler.DEFAULT_PROTOCOL_VERSION);
            sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), headers);
            return;
        }
        String sessionId = request.headers().get(SESSION_HEADER);
        if (sessionId != null && !sessionId.isEmpty()) {
            if (!sessions.containsKey(sessionId)) {
                sendJson(ctx, version, HttpResponseStatus.NOT_FOUND,
                        JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
                return;
            }
        } else {
            sessionId = UUID.randomUUID().toString();
            sessions.put(sessionId, new Session());
        }
        Map<String, String> headers = new ConcurrentHashMap<>();
        headers.put(SESSION_HEADER, sessionId);
        headers.put(PROTOCOL_HEADER, protocolVersion);

        String body = request.content().toString(CharsetUtil.UTF_8);
        Object parsed;
        try {
            parsed = JSONUtils.parse(body);
        } catch (Exception e) {
            sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                    JsonRpcMessages.error(null, ErrorEnum.PARSE_ERROR.getCode()), headers);
            return;
        }
        if (JSONUtils.isArray(parsed)) {
            Object response = jsonRpcServerHandler.handle(body);
            sendJson(ctx, version, HttpResponseStatus.OK, JSONUtils.toString(response), headers);
            return;
        }
        if (!JSONUtils.isObject(parsed)) {
            sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), headers);
            return;
        }
        JSONObject message = (JSONObject) parsed;
        Object id = message.get("id");
        if (id == null) {
            sendJson(ctx, version, HttpResponseStatus.ACCEPTED, "", headers);
            return;
        }
        String method = message.getString("method");
        if (method == null || method.isEmpty()) {
            sendJson(ctx, version, HttpResponseStatus.OK,
                    JsonRpcMessages.error(id, ErrorEnum.INVALID_REQUEST.getCode()), headers);
            return;
        }
        Object params = message.get("params");
        if (McpHandler.handles(method)) {
            McpHandler.Outcome outcome = mcpHandler.handle(id, method, params);
            if (outcome.isAccepted()) {
                sendJson(ctx, version, HttpResponseStatus.ACCEPTED, "", headers);
            } else if (outcome.isStream()) {
                sendEventStream(ctx, version, sessionId, protocolVersion, id, outcome.getStream());
            } else {
                sendJson(ctx, version, HttpResponseStatus.OK, outcome.getJson(), headers);
            }
            return;
        }
        dispatch(ctx, version, sessionId, protocolVersion, id, method, params, headers);
    }

    private void dispatch(ChannelHandlerContext ctx, HttpVersion version, String sessionId, String protocolVersion,
                          Object id, String method, Object params, Map<String, String> headers) {
        Object result;
        try {
            result = jsonRpcServerHandler.invoke(method, params);
        } catch (InvocationTargetException | IllegalAccessException | BeansException e) {
            sendJson(ctx, version, HttpResponseStatus.OK,
                    JsonRpcMessages.error(id, ErrorEnum.METHOD_NOT_FOUND.getCode()), headers);
            return;
        } catch (InvalidParamsException e) {
            sendJson(ctx, version, HttpResponseStatus.OK,
                    JsonRpcMessages.error(id, ErrorEnum.INTERNAL_ERROR.getCode(), e.getMessage()), headers);
            return;
        } catch (MethodNotFoundException e) {
            sendJson(ctx, version, HttpResponseStatus.OK,
                    JsonRpcMessages.error(id, ErrorEnum.METHOD_NOT_FOUND.getCode(), e.getMessage()), headers);
            return;
        }
        if (StreamableUtils.isStreamable(result)) {
            sendEventStream(ctx, version, sessionId, protocolVersion, id, resultStream(id, result));
        } else {
            sendJson(ctx, version, HttpResponseStatus.OK, JsonRpcMessages.result(id, result), headers);
        }
    }

    private void handleGet(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion version = request.protocolVersion();
        String accept = request.headers().get(HttpHeaderNames.ACCEPT);
        if (accept == null || !accept.contains("text/event-stream")) {
            sendJson(ctx, version, HttpResponseStatus.NOT_ACCEPTABLE, "", null);
            return;
        }
        String sessionId = request.headers().get(SESSION_HEADER);
        if (sessionId == null || sessionId.isEmpty()) {
            sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
            return;
        }
        Session session = sessions.get(sessionId);
        if (session == null) {
            sendJson(ctx, version, HttpResponseStatus.NOT_FOUND,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
            return;
        }
        List<BufferedEvent> replay = new ArrayList<>();
        String lastEventId = request.headers().get(LAST_EVENT_ID_HEADER);
        if (lastEventId != null && !lastEventId.isEmpty()) {
            synchronized (session) {
                boolean found = false;
                for (BufferedEvent event : session.events) {
                    if (found) {
                        replay.add(event);
                    } else if (event.id.equals(lastEventId)) {
                        found = true;
                    }
                }
                if (!found) {
                    sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                            JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
                    return;
                }
            }
        }
        HttpResponse response = new DefaultHttpResponse(version, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        response.headers().set(SESSION_HEADER, sessionId);
        HttpUtil.setTransferEncodingChunked(response, true);
        ctx.writeAndFlush(response);
        for (BufferedEvent event : replay) {
            ctx.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(event.frame, CharsetUtil.UTF_8)));
        }
        ctx.flush();
        session.streams.add(ctx.channel());
        scheduleKeepAlive(session, ctx.channel());
        ctx.channel().closeFuture().addListener(future -> {
            session.streams.remove(ctx.channel());
            if (session.streams.isEmpty()) {
                cancelKeepAlive(session);
            }
        });
    }

    private void handleDelete(ChannelHandlerContext ctx, FullHttpRequest request) {
        HttpVersion version = request.protocolVersion();
        String sessionId = request.headers().get(SESSION_HEADER);
        if (sessionId == null || sessionId.isEmpty()) {
            sendJson(ctx, version, HttpResponseStatus.BAD_REQUEST,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
            return;
        }
        Session session = sessions.remove(sessionId);
        if (session == null) {
            sendJson(ctx, version, HttpResponseStatus.NOT_FOUND,
                    JsonRpcMessages.error(null, ErrorEnum.INVALID_REQUEST.getCode()), null);
            return;
        }
        cancelKeepAlive(session);
        for (Channel channel : session.streams) {
            if (channel.isActive()) {
                channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            }
        }
        session.streams.clear();
        sendJson(ctx, version, HttpResponseStatus.NO_CONTENT, "", null);
    }

    private void sendEventStream(ChannelHandlerContext ctx, HttpVersion version, String sessionId,
                                 String protocolVersion, Object id, Iterator<String> messages) {
        HttpResponse response = new DefaultHttpResponse(version, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaderNames.CONNECTION, "keep-alive");
        response.headers().set(SESSION_HEADER, sessionId);
        response.headers().set(PROTOCOL_HEADER, protocolVersion);
        HttpUtil.setTransferEncodingChunked(response, true);
        ctx.writeAndFlush(response);
        Session session = sessions.get(sessionId);
        try {
            while (messages.hasNext()) {
                writeEvent(ctx, session, messages.next());
            }
        } catch (Exception e) {
            log.warn("JsonRpc streamable http stream failed.", e);
            writeEvent(ctx, session, JsonRpcMessages.error(id, ErrorEnum.INTERNAL_ERROR.getCode(), e.getMessage()));
        } finally {
            ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        }
    }

    private void writeEvent(ChannelHandlerContext ctx, Session session, String message) {
        String eventId = UUID.randomUUID().toString();
        String frame = "id: " + eventId + "\nevent: message\ndata: " + message + "\n\n";
        if (session != null) {
            synchronized (session) {
                session.events.addLast(new BufferedEvent(eventId, frame));
                while (session.events.size() > EVENT_BUFFER_LIMIT) {
                    session.events.removeFirst();
                }
            }
        }
        ctx.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(frame, CharsetUtil.UTF_8)));
    }

    private Iterator<String> resultStream(Object id, Object result) {
        Iterator<Object> source = StreamableUtils.toIterator(result);
        return new Iterator<>() {

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
                if (source.hasNext()) {
                    next = JsonRpcMessages.result(id, source.next());
                } else {
                    done = true;
                }
            }
        };
    }

    private void sendJson(ChannelHandlerContext ctx, HttpVersion version, HttpResponseStatus status, String body,
                          Map<String, String> headers) {
        FullHttpResponse response = new DefaultFullHttpResponse(version, status,
                Unpooled.copiedBuffer(body == null ? "" : body, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (headers != null) {
            headers.forEach((name, value) -> response.headers().set(name, value));
        }
        ctx.writeAndFlush(response);
    }

    private String negotiate(String requested) {
        if (requested == null || requested.isEmpty()) {
            return McpHandler.DEFAULT_PROTOCOL_VERSION;
        }
        return McpHandler.PROTOCOL_VERSIONS.contains(requested) ? requested : null;
    }

    private boolean isOriginAllowed(FullHttpRequest request) {
        String origin = request.headers().get(HttpHeaderNames.ORIGIN);
        if (origin == null) {
            return true;
        }
        String host = request.headers().get(HttpHeaderNames.HOST);
        if (host == null) {
            return false;
        }
        try {
            String authority = URI.create(origin).getAuthority();
            return authority != null && authority.equalsIgnoreCase(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private void scheduleKeepAlive(Session session, Channel channel) {
        synchronized (session) {
            if (session.keepAlive != null) {
                return;
            }
            session.keepAlive = channel.eventLoop().scheduleAtFixedRate(() -> {
                for (Channel stream : session.streams) {
                    if (stream.isActive()) {
                        stream.writeAndFlush(new DefaultHttpContent(
                                Unpooled.copiedBuffer(": keepalive\n\n", CharsetUtil.UTF_8)));
                    }
                }
            }, KEEPALIVE_SECONDS, KEEPALIVE_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void cancelKeepAlive(Session session) {
        synchronized (session) {
            if (session.keepAlive != null) {
                session.keepAlive.cancel(false);
                session.keepAlive = null;
            }
        }
    }

    private void registerServices(ApplicationContext applicationContext) {
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            Class<?> type;
            try {
                type = applicationContext.getType(beanName);
            } catch (Exception e) {
                continue;
            }
            String serviceName = serviceName(type);
            if (serviceName == null) {
                continue;
            }
            try {
                mcpHandler.addService(serviceName, applicationContext.getBean(beanName));
            } catch (Exception e) {
                log.warn("Failed to publish the JsonRpc service {} as MCP tools.", serviceName, e);
            }
        }
    }

    private String serviceName(Class<?> type) {
        if (type == null) {
            return null;
        }
        JsonRpcService annotation = AnnotatedElementUtils.findMergedAnnotation(type, JsonRpcService.class);
        if (annotation != null) {
            return annotation.value();
        }
        for (Class<?> interfaceType : type.getInterfaces()) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(interfaceType, JsonRpcService.class);
            if (annotation != null) {
                return annotation.value();
            }
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private static class Session {

        private final Deque<BufferedEvent> events = new ArrayDeque<>();

        private final Set<Channel> streams = ConcurrentHashMap.newKeySet();

        private ScheduledFuture<?> keepAlive;
    }

    private static class BufferedEvent {

        private final String id;

        private final String frame;

        private BufferedEvent(String id, String frame) {
            this.id = id;
            this.frame = frame;
        }
    }
}
