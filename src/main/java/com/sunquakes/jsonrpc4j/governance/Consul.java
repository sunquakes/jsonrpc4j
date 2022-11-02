package com.sunquakes.jsonrpc4j.governance;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.1.0
 * @since : 2022/11/1 7:12 PM
 **/
@Slf4j
public class Consul implements Driver {

    private UriComponents url;

    private ConsulClient client;

    @Override
    public Consul newClient(String url) {
        this.url = UriComponentsBuilder.fromUriString(url).build();
        this.client = getClient();
        return this;
    }

    @Override
    public void register(String name, int port) {
        MultiValueMap<String, String> queryParams = url.getQueryParams();
        NewService newService = new NewService();
        newService.setId(String.format("%s-%s:%d", name, queryParams.get("instanceId"), port));
        newService.setName(name);
        newService.setPort(port);
        client.agentServiceRegister(newService);
    }

    private ConsulClient getClient() {
        ConsulClient client;
        if (url.getPort() == -1) {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()));
        } else {
            client = new ConsulClient(String.format("%s://%s", url.getScheme(), url.getHost()), url.getPort());
        }
        return client;
    }
}
