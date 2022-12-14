package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.exception.JsonRpcException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
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
import java.util.function.Supplier;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/6/2 1:19 PM
 **/
public class JsonRpcServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public JsonRpcServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, Environment environment) {
        super(registry);
        super.setEnvironment(environment);
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                Map<String, Object> annotationAttributes;
                String[] t = metadataReader.getClassMetadata().getInterfaceNames();
                for (int i = 0; i < t.length; i++) {
                    annotationAttributes = metadataReaderFactory.getMetadataReader(t[i])
                            .getAnnotationMetadata()
                            .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                    if (annotationAttributes != null) {
                        return true;
                    }
                }
                return false;
            }
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
        String discoveryDriverName = environment.getProperty("jsonrpc.discovery.driver-name");
        String discoveryUrl = environment.getProperty("jsonrpc.discovery.url");
        boolean hasDiscovery = discoveryDriverName != null && discoveryUrl != null;
        JsonRpcServiceDiscovery jsonRpcServiceDiscovery = null;
        Integer port = environment.getProperty("jsonrpc.server.port", Integer.class);
        String protocol = environment.getProperty("jsonrpc.server.protocol");
        if (hasDiscovery) {
            jsonRpcServiceDiscovery = JsonRpcServiceDiscovery.newInstance(discoveryUrl, discoveryDriverName);
        }
        try {
            BeanDefinitionRegistry registry = getRegistry();
            Assert.notEmpty(basePackages, "At least one base package must be specified");
            Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
            Map<String, Object> annotationAttributes;
            for (String basePackage : basePackages) {
                Set<BeanDefinition> candidates = findCandidateComponents(basePackage);
                for (BeanDefinition candidate : candidates) {
                    String[] t = getMetadataReaderFactory().getMetadataReader(candidate.getBeanClassName()).getAnnotationMetadata().getInterfaceNames();
                    for (int i = 0; i < t.length; i++) {
                        annotationAttributes = getMetadataReaderFactory().getMetadataReader(t[i])
                                .getAnnotationMetadata()
                                .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcService");
                        if (annotationAttributes != null) {
                            String customBeanName = annotationAttributes.get("value").toString();
                            if (StringUtils.hasLength(customBeanName) && checkCandidate(customBeanName, candidate)) {
                                BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, customBeanName);
                                beanDefinitions.add(definitionHolder);
                                registerBeanDefinition(definitionHolder, registry);
                                // Register service
                                if (hasDiscovery) {
                                    JsonRpcServiceDiscovery finalJsonRpcServiceDiscovery = jsonRpcServiceDiscovery;
                                    String ip = hostname;
                                    JsonRpcServiceDiscovery.addService(() -> {
                                        finalJsonRpcServiceDiscovery.getDriver().register(customBeanName, protocol, ip, port);
                                        return true;
                                    });
                                }
                            }
                        }
                    }
                }
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
            return null;
        } catch (SocketException e) {
            return null;
        }
    }
}
