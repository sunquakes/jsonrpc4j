package com.sunquakes.jsonrpc4j.spring.boot;

import com.sunquakes.jsonrpc4j.discovery.Consul;
import com.sunquakes.jsonrpc4j.spring.JsonRpcServiceDiscovery;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ArgsDto;
import com.sunquakes.jsonrpc4j.spring.boot.dto.ResultDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = JsonRpcApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("consul")
class ConsulTest {

    @Value("${jsonrpc.server.protocol}")
    private String protocol;

    @Value("${jsonrpc.server.port}")
    private int port;

    private static MockedStatic<JsonRpcServiceDiscovery> mockStatic;

    public ConsulTest() {
        Consul consul = mock(Consul.class);
        when(consul.get(anyString())).thenReturn("localhost:" + 3208);
        JsonRpcServiceDiscovery instanse = mock(JsonRpcServiceDiscovery.class);
        when(instanse.getDriver()).thenReturn(consul);
        mockStatic = mockStatic(JsonRpcServiceDiscovery.class);
        mockStatic.when(() -> JsonRpcServiceDiscovery.newInstance(anyString(), anyString())).thenReturn(instanse);
    }

    @AfterEach
    public void releaseMocks() {
        mockStatic.close();
    }

    @Resource
    private IJsonRpcClient jsonRpcClient;

    @Test
    void testGetConfiguration() {
        assertEquals("tcp", protocol);
        assertEquals(3208, port);
    }

    @Test
    void testRequest() {
        // test request
        {
            assertEquals(3, jsonRpcClient.add(1, 2));
            assertEquals(7, jsonRpcClient.add(3, 4));
            assertEquals(7, jsonRpcClient.add(5, 2));

            assertEquals(1, jsonRpcClient.sub(3, 2));
            assertEquals(2, jsonRpcClient.sub(7, 5));

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
