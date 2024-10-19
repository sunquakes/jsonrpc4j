package com.sunquakes.jsonrpc4j.discovery;

import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.utils.AddressUtils;
import com.sunquakes.jsonrpc4j.utils.JSONUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 2.1.0
 * @since 2.1.0
 **/
@Slf4j
public class Consul implements Driver {

    private UriComponents url;

    private NettyHttpClient client;

    private boolean check; // Whether to enable health check.

    private String checkInterval = "60s"; // Health check interval.

    private String instanceId; // Identify the same services in the different node.

    private static final String CHECK_FIELD = "check";

    private static final String CHECK_INTERVAL_FIELD = "checkInterval";

    private static final String INSTANCE_ID_FIELD = "instanceId";

    @Override
    public Consul newClient(String url) {
        this.url = UriComponentsBuilder.fromUriString(url).build();
        this.client = getClient();
        return this;
    }

    @Override
    public boolean register(String name, String protocol, String hostname, int port) {
        String id;
        if (instanceId != null) {
            id = String.format("%s-%s:%d", name, instanceId, port);
        } else {
            id = AddressUtils.getUrl(name, port);
        }
        if (check) {
            checkRegister(protocol, hostname, port, name);
        }

        RegisterService service = new RegisterService(id, name, port, hostname);
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents fullUrl = builder.uriComponents(this.url)
                .path("/v1/agent/service/register")
                .build();
        try {
            FullHttpResponse res = client.put(fullUrl.getPath() + "?" + fullUrl.getQuery(), JSONUtils.toString(service));
            if (!res.status().equals(HttpResponseStatus.OK)) {
                return false;
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void checkRegister(String protocol, String hostname, int port, String name) {
        protocol = protocol.toUpperCase();
        Check serviceCheck = new Check();
        serviceCheck.setName(name);
        if (protocol.equals(JsonRpcProtocol.TCP.name())) {
            serviceCheck.setTCP(AddressUtils.getUrl(hostname, port));
        } else {
            serviceCheck.setHTTP(String.format("%s://%s:%d", protocol, hostname, port));
        }
        serviceCheck.setInterval(checkInterval);
        // Set the init status passing
        serviceCheck.setStatus("passing");
        // Set the http method
        serviceCheck.setMethod("GET");
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents fullUrl = builder.uriComponents(this.url)
                .path("/v1/agent/check/register")
                .build();
        try {
            FullHttpResponse res = client.put(fullUrl.getPath() + "?" + fullUrl.getQuery(), JSONUtils.toString(serviceCheck));
            if (!res.status().equals(HttpResponseStatus.OK)) {
                log.error("Health check register failed.");
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String get(String name) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents fullUrl = builder.uriComponents(this.url)
                .path("/v1/agent/health/service/name/" + name)
                .build();
        try {
            FullHttpResponse res = client.get(fullUrl.getPath() + "?" + fullUrl.getQuery());
            if (!res.status().equals(HttpResponseStatus.OK)) {
                return "";
            }
            ByteBuf content = res.content();
            String body = content.toString(CharsetUtil.UTF_8);
            List<HealthService> healthyServices = JSONUtils.parseList(body, HealthService.class);
            return healthyServices.stream().map(item -> AddressUtils.getUrl(item.getService().getAddress(), item.getService().getPort())).collect(Collectors.joining(","));
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            log.error(e.getMessage(), e);
            return "";
        }
    }

    private NettyHttpClient getClient() {
        if (url.getPort() == -1) {
            client = new NettyHttpClient(String.format("%s://%s", url.getScheme(), url.getHost()));
        } else {
            client = new NettyHttpClient(String.format("%s://%s:%d", url.getScheme(), url.getHost(), url.getPort()));
        }

        // Deserialize url parameters.
        MultiValueMap<String, String> queryParams = url.getQueryParams();
        if (queryParams.containsKey(CHECK_FIELD)) {
            check = Boolean.parseBoolean(queryParams.getFirst(CHECK_FIELD));
        }
        if (queryParams.containsKey(CHECK_INTERVAL_FIELD) && StringUtils.hasLength(queryParams.getFirst(CHECK_INTERVAL_FIELD))) {
            checkInterval = queryParams.getFirst(CHECK_INTERVAL_FIELD);
        }
        if (queryParams.containsKey(INSTANCE_ID_FIELD) && StringUtils.hasLength(queryParams.getFirst(INSTANCE_ID_FIELD))) {
            instanceId = queryParams.getFirst(INSTANCE_ID_FIELD);
        }
        return client;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuppressWarnings({"java:S116"})
    static public class HealthService {

        private String AggregatedStatus;

        private NewService Service;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuppressWarnings({"java:S116"})
    static public class NewService {

        private String ID;

        private String Service;

        private Integer Port;

        private String Address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuppressWarnings({"java:S116"})
    static public class RegisterService {

        private String ID;

        private String Name;

        private Integer Port;

        private String Address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuppressWarnings({"java:S116"})
    static public class Check {

        private String ID;

        private String Name;

        private String Status;

        private String ServiceID;

        private String HTTP;

        private String Method;

        private String TCP;

        private String Interval;

        private String Timeout;
    }
}
