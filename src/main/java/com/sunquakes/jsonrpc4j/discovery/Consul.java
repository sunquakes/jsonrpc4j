package com.sunquakes.jsonrpc4j.discovery;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.utils.AddressUtils;
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
            checkRegister(protocol, hostname, port);
        }

        RegisterService service = new RegisterService(id, name, port, hostname);
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        UriComponents fullUrl = builder.uriComponents(this.url)
                .path("/v1/agent/service/register")
                .build();
        try {
            FullHttpResponse res = client.put(fullUrl.toUriString(), JSONObject.toJSONString(service));
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

    private void checkRegister(String protocol, String hostname, int port) {
        protocol = protocol.toUpperCase();
        Check serviceCheck = new Check();
        if (protocol.equals(JsonRpcProtocol.TCP.name())) {
            serviceCheck.setTcp(AddressUtils.getUrl(hostname, port));
        } else {
            serviceCheck.setHttp(String.format("%s://%s:%d", protocol, hostname, port));
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
            FullHttpResponse res = client.put(fullUrl.toUriString(), JSONObject.toJSONString(serviceCheck));
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
            FullHttpResponse res = client.get(fullUrl.toUriString());
            if (!res.status().equals(HttpResponseStatus.OK)) {
                return "";
            }
            ByteBuf content = res.content();
            String body = content.toString(CharsetUtil.UTF_8);
            List<HealthService> healthyServices = JSONArray.parseArray(body, HealthService.class);
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
    public class HealthService {

        @JSONField(name = "AggregatedStatus")
        private String aggregatedStatus;

        @JSONField(name = "Service")
        private NewService service;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class NewService {

        @JSONField(name = "ID")
        private String id;

        @JSONField(name = "Service")
        private String service;

        @JSONField(name = "Port")
        private Integer port;

        @JSONField(name = "Address")
        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class RegisterService {

        @JSONField(name = "ID")
        private String id;

        @JSONField(name = "Name")
        private String name;

        @JSONField(name = "Port")
        private Integer port;

        @JSONField(name = "Address")
        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Check {

        @JSONField(name = "ID")
        private String id;

        @JSONField(name = "Name")
        private String name;

        @JSONField(name = "Status")
        private String status;

        @JSONField(name = "ServiceID")
        private String serviceID;

        @JSONField(name = "HTTP")
        private String http;

        @JSONField(name = "Method")
        private String method;

        @JSONField(name = "TCP")
        private String tcp;

        @JSONField(name = "Interval")
        private String interval;

        @JSONField(name = "Timeout")
        private String timeout;
    }
}
