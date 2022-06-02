package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcService;
import com.sunquakes.jsonrpc4j.ProtocolEnum;
import com.sunquakes.jsonrpc4j.server.JsonRpcHttpServer;
import com.sunquakes.jsonrpc4j.server.JsonRpcTcpServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.spring
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
public class JsonRpcServiceBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor, EnvironmentAware {

    private final String SERVICE_SUFFIX = "Service";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private static Map<String, String> findBeanDefinitions(ConfigurableListableBeanFactory beanFactory) {
        final Map<String, String> beanNames = new HashMap<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            beanNames.put(beanName, beanName);
        }
        return beanNames;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        Map<String, String> servicePathToBeanName = findBeanDefinitions(defaultListableBeanFactory);
        for (Map.Entry<String, String> entry : servicePathToBeanName.entrySet()) {
            if (defaultListableBeanFactory == null) continue;
            registerServiceProxy(defaultListableBeanFactory, entry.getValue());
        }
    }

    private void registerServiceProxy(DefaultListableBeanFactory defaultListableBeanFactory, String beanName) {
        if (defaultListableBeanFactory == null) {
            return;
        }
        BeanDefinition serviceBeanDefinition = findBeanDefinition(defaultListableBeanFactory, beanName);
        try {
            for (Class<?> currentInterface : getBeanInterfaces(serviceBeanDefinition, defaultListableBeanFactory.getBeanClassLoader())) {
                System.out.println("77777777777");
                System.out.println(currentInterface.getName());
                System.out.println(currentInterface.isAnnotationPresent(JsonRpcService.class));
                if (currentInterface.isAnnotationPresent(JsonRpcService.class)) {
                    BeanDefinitionBuilder serviceBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanName(beanName));
                    String key = getPathByBeanName(beanName, SERVICE_SUFFIX);
                    System.out.println(key);
                    defaultListableBeanFactory.registerBeanDefinition(key, serviceBuilder.getBeanDefinition());

                    String protocol = environment.getProperty("jsonrpc.server.protocol");
                    String serverBeanName = String.format("%s_%s", "JsonRpcServer", protocol);
                    if (protocol != null && !defaultListableBeanFactory.containsBeanDefinition(serverBeanName)) {
                        BeanDefinitionBuilder serverBuilder;
                        if (protocol.equals(ProtocolEnum.Tcp.getName())) {
                            serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcTcpServer.class);
                        } else if (protocol.equals(ProtocolEnum.Http.getName())) {
                            serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(JsonRpcHttpServer.class);
                        } else {
                            throw new IllegalArgumentException("Invalid protocol.");
                        }
                        defaultListableBeanFactory.registerBeanDefinition(serverBeanName, serverBuilder.getBeanDefinition());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(beanName);
            // e.printStackTrace();
        }
    }

    private BeanDefinition findBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
        if (beanFactory.containsLocalBean(beanName)) {
            return beanFactory.getBeanDefinition(beanName);
        }
        BeanFactory parentBeanFactory = beanFactory.getParentBeanFactory();
        if (parentBeanFactory != null && ConfigurableListableBeanFactory.class.isInstance(parentBeanFactory)) {
            return findBeanDefinition((ConfigurableListableBeanFactory) parentBeanFactory, beanName);
        }
        throw new NoSuchBeanDefinitionException(beanName);
    }

    private Class<?>[] getBeanInterfaces(BeanDefinition serviceBeanDefinition, ClassLoader beanClassLoader) {
        String beanClassName = serviceBeanDefinition.getBeanClassName();
        if (beanClassName == null || beanClassLoader == null) {
            return null;
        }
        try {
            Class<?> beanClass = forName(beanClassName, beanClassLoader);
            return getAllInterfacesForClass(beanClass, beanClassLoader);
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException(format("Cannot find bean class '%s'.", beanClassName), e);
        }
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

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathBeanDefinitionScanner scanner = new JsonRpcServiceClassPathBeanDefinitionScanner(registry);
        scanner.scan("com.sunquakes.jsonrpc4j");
    }
}
