package com.sunquakes.jsonrpc4j;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.client.IJsonRpcClient;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.service.JsonRpcServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcClientTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testGetBean() throws IOException {
        {
            Object bean = applicationContext.getBean("client");
            assertSame(IJsonRpcClient.class, bean.getClass());
        }
    }
}
