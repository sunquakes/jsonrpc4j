package com.sunquakes.jsonrpc4j.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/6/3 5:17 PM
 **/
public class JsonRpcServiceBeanNameGenerator implements BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        return getPathByBeanName(definition.getBeanClassName(), "Service");
    }

    private String getPathByBeanName(String beanName, String suffix) {
        String[] arr = beanName.split("\\.");
        String name = arr[arr.length - 1];
        arr = name.split(suffix);
        return arr[0];
    }

    private String getBeanName(String beanName) {
        String[] arr = beanName.split("\\#");
        return arr[0];
    }
}
