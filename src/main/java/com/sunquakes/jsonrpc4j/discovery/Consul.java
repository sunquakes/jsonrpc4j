package com.sunquakes.jsonrpc4j.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.utils.AddressUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/1 7:12 PM
 **/
@Slf4j
public class Consul implements Driver {

    private UriComponents url;

    private ConsulClient client;

    private String token; // Request token.

    private boolean check; // Whether to enable health check.

    private String checkInterval = "60s"; // Health check interval.

    private String instanceId; // Identify the same services in the different node.

    private static final String CHECK_FIELD = "check";

    private static final String TOKEN_FIELD = "token";

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
        NewService newService = new NewService();
        String id;
        if (instanceId != null) {
            id = String.format("%s-%s:%d", name, instanceId, port);
        } else {
            id = AddressUtils.getUrl(name, port);
        }
        newService.setId(id);
        newService.setName(name);
        newService.setPort(port);
        if (hostname != null) {
            newService.setAddress(hostname);
        }
        if (check) {
            protocol = protocol.toUpperCase();
            NewService.Check serviceCheck = new NewService.Check();
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
            newService.setCheck(serviceCheck);
        }

        try {
            if (token != null) {
                client.agentServiceRegister(newService, token);
            } else {
                client.agentServiceRegister(newService);
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public String get(String name) {
        HealthServicesRequest.Builder builder = HealthServicesRequest.newBuilder()
                .setPassing(true)
                .setQueryParams(QueryParams.DEFAULT);
        if (token != null) {
            builder.setToken(token);
        }
        HealthServicesRequest request = builder.build();
        Response<List<HealthService>> healthyServices = client.getHealthServices(name, request);
        return healthyServices.getValue().stream().map(item -> AddressUtils.getUrl(item.getService().getAddress(), item.getService().getPort())).collect(Collectors.joining(","));
    }

    private ConsulClient getClient() {
        if (url.getPort() == -1) {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()));
        } else {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()), url.getPort());
        }

        // Deserialize url parameters.
        MultiValueMap<String, String> queryParams = url.getQueryParams();
        if (queryParams.containsKey(TOKEN_FIELD) && StringUtils.hasLength(queryParams.getFirst(TOKEN_FIELD))) {
            token = queryParams.getFirst(TOKEN_FIELD);
        }
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
}
