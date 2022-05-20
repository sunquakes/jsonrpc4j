package com.sunquakes.jsonrpc4j.client;

import com.sunquakes.jsonrpc4j.client.IJsonRpcClient;
import com.sunquakes.jsonrpc4j.spring.JsonRpcClientImportBeanDefinitionRegistrar;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.junit.Assert.assertSame;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class JsonRpcClientTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testGetBean() throws IOException {
        {
            IJsonRpcClient bean = (IJsonRpcClient) applicationContext.getBean("client");
            // System.out.println(bean.add(1, 2));
        }
    }
}
