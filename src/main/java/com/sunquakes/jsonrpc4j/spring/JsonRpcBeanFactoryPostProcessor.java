package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcClient;
import com.sunquakes.jsonrpc4j.JsonRpcService;
import com.sunquakes.jsonrpc4j.client.JsonRpcClientInvocationHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.springframework.util.ClassUtils.forName;
import static org.springframework.util.ClassUtils.getAllInterfacesForClass;

public class JsonRpcBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private final String SERVICE_SUFFIX = "Service";

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
            registerServiceProxy(defaultListableBeanFactory, entry.getValue());
        }
    }

    private void registerServiceProxy(DefaultListableBeanFactory defaultListableBeanFactory, String beanName) {
        BeanDefinition serviceBeanDefinition = findBeanDefinition(defaultListableBeanFactory, beanName);
        for (Class<?> currentInterface : getBeanInterfaces(serviceBeanDefinition, defaultListableBeanFactory.getBeanClassLoader())) {
            if (currentInterface.isAnnotationPresent(JsonRpcService.class)) {
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(getBeanName(beanName));
                String key = getPathByBeanName(beanName, SERVICE_SUFFIX);
                defaultListableBeanFactory.registerBeanDefinition(key, builder.getBeanDefinition());
            } else if (currentInterface.isAnnotationPresent(JsonRpcClient.class)) {
                System.out.println(1234567890);
                // Class proxy = (Class) Proxy.newProxyInstance(defaultListableBeanFactory.getBeanClassLoader(), currentInterface.getInterfaces(), new JsonRpcClientInvocationHandler());
                // BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(proxy);
                // defaultListableBeanFactory.registerBeanDefinition("client", builder.getBeanDefinition());
            }
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
}
