package com.sunquakes.jsonrpc4j.discovery;

import com.alibaba.fastjson.JSONObject;
import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.2.0
 * @since : 2023/1/15 10:34 AM
 **/
@Slf4j
public class Nacos implements Driver {

    private UriComponents url;

    private CloseableHttpClient client = HttpClients.createDefault();

    @Override
    public Nacos newClient(String url) {
        this.url = UriComponentsBuilder.fromUriString(url).build();
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
                .queryParam("ephemeral", "false")
                .build();

        HttpPost post = new HttpPost(url.toString());
        try {
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() != 200) {
                new JsonRpcException("Failed to register to nacos.");
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
            if (res.getStatusLine().getStatusCode() != 200) {
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

    @Data
    public class GetResp {
        private List<Service> hosts;
    }

    @Data
    public class Service {
        private String instanceId;
        private boolean healthy;
        private Integer port;
        private String ip;
    }
}
