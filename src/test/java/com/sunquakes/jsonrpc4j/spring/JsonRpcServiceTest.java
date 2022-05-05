package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.JsonRpcServiceBean;
import com.sunquakes.jsonrpc4j.spring.server.JsonRpcServiceImpl;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:serviceApplicationContext.xml")
public class JsonRpcServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testContext() {
        assertNotNull(applicationContext);
    }

    @Test
    public void testGetBean() throws IOException {
        {
            Object bean = applicationContext.getBean("JsonRpcService");
            assertSame(JsonRpcServiceBean.class, bean.getClass());
        }

        {
            Map<String, ? extends JsonRpcServiceImpl> beans = applicationContext.getBeansOfType(JsonRpcServiceImpl.class);
            Set<Class<? extends JsonRpcServiceImpl>> beanClasses = new HashSet<>();
            for (JsonRpcServiceImpl jsonRpcService : beans.values()) {
                beanClasses.add(jsonRpcService.getClass());
            }
            assertTrue(beanClasses.contains(JsonRpcServiceImpl.class));
        }
    }
}