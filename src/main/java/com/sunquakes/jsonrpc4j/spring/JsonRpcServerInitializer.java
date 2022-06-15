package com.sunquakes.jsonrpc4j.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/6/1 1:04 PM
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
