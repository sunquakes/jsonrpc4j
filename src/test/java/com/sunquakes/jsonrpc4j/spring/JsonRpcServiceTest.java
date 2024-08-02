package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.service.JsonRpcServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-http.properties")
@ContextConfiguration("classpath:applicationContext.xml")
class JsonRpcServiceTest {

    @Value("${jsonrpc.server.protocol}")
    private final String protocol = null;

    @Resource
    private ApplicationContext applicationContext;

    @Test
    void testContext() {
        assertNotNull(applicationContext);
    }

    @Test
    void testGetBean() {
        {
            Object bean = applicationContext.getBean("JsonRpc");
            assertSame(JsonRpcServiceImpl.class, bean.getClass());
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

    @Test
    void testGetConfiguration() {
        assertEquals("http", protocol);
    }
}
