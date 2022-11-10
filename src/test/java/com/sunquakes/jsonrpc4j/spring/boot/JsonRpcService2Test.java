package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.client.JsonRpcLoadBalancer;
import com.sunquakes.jsonrpc4j.discovery.Consul;
import com.sunquakes.jsonrpc4j.spring.JsonRpcServiceDiscovery;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ArgsDto;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ResultDto;
import io.netty.channel.Channel;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.util.concurrent.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.runner.RunWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring.boot
 * @Author: Robert
 * @CreateTime: 2022/5/30 12:48 PM
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = JsonRpcApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JsonRpcService2Test {

    @Value("${jsonrpc.server.protocol}")
    private String protocol;

    @Value("${jsonrpc.server.port}")
    private int port;

    private static MockedStatic<JsonRpcServiceDiscovery> mockStatic;

    public JsonRpcService2Test() {
        Consul consul = mock(Consul.class);
        when(consul.get(anyString())).thenReturn("localhost:" + 3202);
        JsonRpcServiceDiscovery instanse = mock(JsonRpcServiceDiscovery.class);
        when(instanse.getDriver()).thenReturn(consul);
        mockStatic = mockStatic(JsonRpcServiceDiscovery.class);
        mockStatic.when(() -> JsonRpcServiceDiscovery.newInstance(anyString(), anyString())).thenReturn(instanse);
    }

    @After
    public void releaseMocks() throws Exception {
        mockStatic.close();
    }

    @Autowired
    private IJsonRpcClient jsonRpcClient;

    @Test
    public void testGetConfiguration() {
        assertEquals(protocol, "tcp");
        assertEquals(port, 3202);
    }

    @Test
    public void testRequest() throws IOException, InterruptedException {
        // test request
        {
            assertEquals(jsonRpcClient.add(1, 2), 3);
            assertEquals(jsonRpcClient.add(3, 4), 7);
            assertEquals(jsonRpcClient.add(5, 2), 7);

            assertEquals(jsonRpcClient.sub(3, 2), 1);
            assertEquals(jsonRpcClient.sub(7, 5), 2);

            ArgsDto args = new ArgsDto();
            args.setA(8);
            args.setB(9);
            ResultDto result = jsonRpcClient.add2(args);
            assertEquals(17, result.getC());

            ArgsDto innerArgs = new ArgsDto();
            innerArgs.setA(10);
            innerArgs.setB(11);
            args.setArgs(innerArgs);
            result = jsonRpcClient.add3(args);
            assertEquals(21, result.getResult().getC());
        }
    }
}
