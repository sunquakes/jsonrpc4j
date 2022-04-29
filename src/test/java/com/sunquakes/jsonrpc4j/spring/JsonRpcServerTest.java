package com.sunquakes.jsonrpc4j.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:serverApplicationContext.xml")
public class JsonRpcServerTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testContext() {
        assertNotNull(applicationContext);
    }
}
