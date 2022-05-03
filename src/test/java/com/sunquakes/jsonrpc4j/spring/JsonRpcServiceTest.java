package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.spring.server.JsonRpcServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:serverApplicationContext.xml")
public class JsonRpcServiceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testContext() {
        assertNotNull(applicationContext);
    }

    @Test
    public void testGetBean() {
        {
            Object bean = applicationContext.getBean("JsonRpcService");
            assertSame(JsonRpcHttpServer.class, bean.getClass());
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
