package com.sunquakes.jsonrpc4j.discovery;

import com.alibaba.fastjson.JSONObject;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.2.0
 * @since : 2023/1/15 10:34 AM
 **/
@Slf4j
public class Nacos implements Driver {

    private static final int STATUS_CODE_SUCCESS = 200;

    private static final int HEARTBEAT_INTERVAL = 5000;

    private static final String EPHEMERAL_KEY = "ephemeral";

    private static final String IS_EPHEMERAL = "true";

    private UriComponents url;

    private CloseableHttpClient client = HttpClients.createDefault();

    private String ephemeral = "true";

    private List<Map.Entry<String, Service>> heartbeatList = new ArrayList<>();

    @Override
    public Nacos newClient(String url) {
        this.url = UriComponentsBuilder.fromUriString(url).build();
        if (this.url.getQueryParams().containsKey(EPHEMERAL_KEY)) {
            ephemeral = this.url.getQueryParams().getFirst("ephemeral");
        }
        return this;
    }

    @Override
    public void register(String name, String protocol, String hostname, int port) {
        UriComponents url = this.url;
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        url = builder.uriComponents(url)
                .path("/nacos/v1/ns/instance")
                .queryParam("serviceName", name)
                .queryParam("ip", hostname)
                .queryParam("port", port)
                .queryParam("ephemeral", ephemeral)
                .build();

        HttpPost post = new HttpPost(url.toString());
        try {
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != STATUS_CODE_SUCCESS) {
                new JsonRpcException("Failed to register to nacos.");
            }
            if (IS_EPHEMERAL.equals(ephemeral)) {
                registerHeartbeat(name, hostname, port);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String get(String name) {
        UriComponents url = this.url;
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        url = builder.uriComponents(url)
                .path("/nacos/v1/ns/instance/list")
                .queryParam("serviceName", name)
                .build();

        HttpGet get = new HttpGet(url.toString());
        try {
            HttpResponse res = client.execute(get);
            if (res.getStatusLine().getStatusCode() != STATUS_CODE_SUCCESS) {
                new JsonRpcException("Failed to get the service list from nacos.");
            }
            String json = EntityUtils.toString(res.getEntity());
            GetResp resp = JSONObject.parseObject(json, GetResp.class);
            return resp.getHosts().stream().filter(item -> item.healthy).map(item -> String.format("%s:%d", item.getIp(), item.getPort())).collect(Collectors.joining(","));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return name;
    }

    public String beat(String serviceName, String ip, int port) {
        UriComponents url = this.url;
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        url = builder.uriComponents(url)
                .path("/nacos/v1/ns/instance/beat")
                .queryParam("serviceName", serviceName)
                .queryParam("ip", ip)
                .queryParam("port", port)
                .queryParam("ephemeral", ephemeral) .build();

        HttpPut put = new HttpPut(url.toString());
        try {
            HttpResponse res = client.execute(put);
            if (res.getStatusLine().getStatusCode() != STATUS_CODE_SUCCESS) {
                new JsonRpcException("Failed to send heartbeat to nacos.");
            }
            String json = EntityUtils.toString(res.getEntity());
            return json;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private void registerHeartbeat(String serviceName, String ip, int port) {
        if (heartbeatList.isEmpty()) {
            heartbeat();
        }
        heartbeatList.add(new AbstractMap.SimpleEntry<>(serviceName, new Service(ip, port, null, null)));
    }

    private void heartbeat() {
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(() -> {
            while (true) {
                try {
                    heartbeatList.forEach(item -> {
                        Service service = item.getValue();
                        beat(item.getKey(), service.getIp(), service.getPort());
                    });
                    Thread.sleep(HEARTBEAT_INTERVAL);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    @Data
    public class GetResp {
        private List<Service> hosts;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class Service {
        private String ip;
        private Integer port;
        private String instanceId;
        private Boolean healthy;
    }
}
