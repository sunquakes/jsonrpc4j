package com.sunquakes.jsonrpc4j.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Robert, sunquakes@outlook.com
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

    @Override
    public Consul newClient(String url) {
        this.url = UriComponentsBuilder.fromUriString(url).build();
        this.client = getClient();
        return this;
    }

    @Override
    public void register(String name, String protocol, String hostname, int port) {
        NewService newService = new NewService();
        String id;
        if (instanceId != null) {
            id = String.format("%s-%s:%d", name, instanceId, port);
        } else {
            id = String.format("%s:%d", name, port);
        }
        newService.setId(id);
        newService.setName(name);
        newService.setPort(port);
        if (hostname != null) {
            newService.setAddress(hostname);
        }
        if (check) {
            NewService.Check serviceCheck = new NewService.Check();
            if (protocol.equals(JsonRpcProtocol.tcp.name())) {
                serviceCheck.setTcp(String.format("%s:%d", hostname, port));
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

        // Register the service;
        if (token != null) {
            client.agentServiceRegister(newService, token);
        } else {
            client.agentServiceRegister(newService);
        }
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
        String url = healthyServices.getValue().stream().map(item -> String.format("%s:%d", item.getService().getAddress(), item.getService().getPort())).collect(Collectors.joining(","));
        return url;
    }

    private ConsulClient getClient() {
        ConsulClient client;
        if (url.getPort() == -1) {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()));
        } else {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()), url.getPort());
        }

        // Deserialize url parameters.
        MultiValueMap<String, String> queryParams = url.getQueryParams();
        if (queryParams.containsKey("token") && StringUtils.hasLength(queryParams.getFirst("token"))) {
            token = queryParams.getFirst("token");
        }
        if (queryParams.containsKey("check") && queryParams.getFirst("check").equals("true")) {
            check = true;
        }
        if (queryParams.containsKey("checkInterval") && StringUtils.hasLength(queryParams.getFirst("checkInterval"))) {
            checkInterval = queryParams.getFirst("checkInterval");
        }
        if (queryParams.containsKey("instanceId") && StringUtils.hasLength(queryParams.getFirst("instanceId"))) {
            instanceId = queryParams.getFirst("instanceId");
        }
        return client;
    }
}
