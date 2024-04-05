package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Shing Rui <sunquakes@outlook.com>
 * @version 3.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    private static final String ANNOTATION_VALUE_KEY = "value";

    public JsonRpcServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment) {
        super(registry);
        super.setEnvironment(environment);
        addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> {
            Map<String, Object> annotationAttributes;
            String[] t = metadataReader.getClassMetadata().getInterfaceNames();
            for (String className : t) {
                annotationAttributes = metadataReaderFactory.getMetadataReader(className)
                        .getAnnotationMetadata()
                        .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                if (annotationAttributes != null) {
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Environment environment = getEnvironment();
        String hostname = environment.getProperty("jsonrpc.discovery.hostname");
        if (!StringUtils.hasLength(hostname)) {
            hostname = getHostname();
        }
        if (!StringUtils.hasLength(hostname)) {
            throw new JsonRpcException("Failed to get hostname.");
        }
        try {
            Assert.notEmpty(basePackages, "At least one base package must be specified");
            Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
                handleCandidates(candidates, environment, hostname, beanDefinitions);
            }
            return beanDefinitions;
        } catch (IOException e) {
            throw new JsonRpcException(e.getMessage());
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && (metadata.isConcrete() || metadata.isInterface());
    }

    private String getHostname() {
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                        return ip.getHostAddress();
                    }
                }
            }
            return "";
        } catch (SocketException e) {
            return "";
        }
    }

    private void handleCandidates(Set<BeanDefinition> candidates, Environment environment, String hostname, Set<BeanDefinitionHolder> beanDefinitions) throws IOException {
        BeanDefinitionRegistry registry = getRegistry();
        String discoveryDriverName = environment.getProperty("jsonrpc.discovery.driver-name");
        String discoveryUrl = environment.getProperty("jsonrpc.discovery.url");
        boolean hasDiscovery = discoveryDriverName != null && discoveryUrl != null;
        JsonRpcServiceDiscovery jsonRpcServiceDiscovery = null;
        Integer port = environment.getProperty("jsonrpc.server.port", Integer.class);
        String protocol = environment.getProperty("jsonrpc.server.protocol");
        if (hasDiscovery) {
            jsonRpcServiceDiscovery = JsonRpcServiceDiscovery.newInstance(discoveryUrl, discoveryDriverName);
        }
        Map<String, Object> annotationAttributes;
        for (BeanDefinition candidate : candidates) {
            String beanClassName = candidate.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            String[] t = getMetadataReaderFactory().getMetadataReader(beanClassName).getAnnotationMetadata().getInterfaceNames();
            for (int i = 0; i < t.length; i++) {
                annotationAttributes = getMetadataReaderFactory().getMetadataReader(t[i])
                        .getAnnotationMetadata()
                        .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                if (annotationAttributes == null || annotationAttributes.get(ANNOTATION_VALUE_KEY) == null || !StringUtils.hasLength(annotationAttributes.get(ANNOTATION_VALUE_KEY).toString()) || !checkCandidate(annotationAttributes.get(ANNOTATION_VALUE_KEY).toString(), candidate)) {
                    continue;
                }
                String customBeanName = annotationAttributes.get(ANNOTATION_VALUE_KEY).toString();

                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, customBeanName);
                beanDefinitions.add(definitionHolder);
                registerBeanDefinition(definitionHolder, registry);
                // Register service
                if (hasDiscovery) {
                    addService(protocol, hostname, port, customBeanName, jsonRpcServiceDiscovery);
                }
            }
        }
    }

    private void addService(String protocol, String hostname, Integer port, String customBeanName, JsonRpcServiceDiscovery jsonRpcServiceDiscovery) {
        JsonRpcServiceDiscovery finalJsonRpcServiceDiscovery = jsonRpcServiceDiscovery;
        String ip = hostname;
        JsonRpcServiceDiscovery.addService(() -> {
            Future<?> future = JsonRpcServiceDiscovery.retryThread.scheduleWithFixedDelay(() -> {
                boolean res = finalJsonRpcServiceDiscovery.getDriver().register(customBeanName, protocol, ip, port);
                if (!res) {
                    return;
                }
                Future<?> f = JsonRpcServiceDiscovery.retryMap.get(customBeanName);
                if (f == null) {
                    return;
                }
                JsonRpcServiceDiscovery.retryMap.remove(customBeanName);
                f.cancel(true);
            }, JsonRpcServiceDiscovery.REGISTRY_RETRY_INTERVAL, JsonRpcServiceDiscovery.REGISTRY_RETRY_INTERVAL, TimeUnit.MILLISECONDS);
            JsonRpcServiceDiscovery.retryMap.put(customBeanName, future);
            return true;
        });
    }
}
