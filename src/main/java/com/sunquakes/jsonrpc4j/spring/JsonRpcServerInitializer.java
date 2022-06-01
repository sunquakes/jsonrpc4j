package com.sunquakes.jsonrpc4j.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/6/1 1:04 PM
 **/
public class JsonRpcServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        JsonRpcServiceBeanFactoryPostProcessor jsonRpcServiceBeanFactoryPostProcessor = new JsonRpcServiceBeanFactoryPostProcessor();
        jsonRpcServiceBeanFactoryPostProcessor.setEnvironment(environment);
        applicationContext.addBeanFactoryPostProcessor(jsonRpcServiceBeanFactoryPostProcessor);
    }
}
