package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.JsonRpcProtocol;
import com.sunquakes.jsonrpc4j.client.JsonRpcClientInterface;
import com.sunquakes.jsonrpc4j.client.JsonRpcHttpClient;
import com.sunquakes.jsonrpc4j.client.JsonRpcTcpClient;
import com.sunquakes.jsonrpc4j.config.Config;
import com.sunquakes.jsonrpc4j.config.ConfigEntry;
import com.sunquakes.jsonrpc4j.discovery.Driver;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author Shing Rui <a href="mailto:sunquakes@outlook.com">sunquakes@outlook.com</a>
 * @version 3.0.0
 * @since 1.0.0
 **/
@Slf4j
public class JsonRpcClientClassPathScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

    public JsonRpcClientClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment, BeanDefinitionRegistry beanDefinitionRegistry) {
        super(useDefaultFilters);
        super.addIncludeFilter((MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) -> {
            Map<String, Object> annotationAttributes = metadataReader
                    .getAnnotationMetadata()
                    .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcClient");

            if (annotationAttributes == null)
                return false;

            Class<?> interfaceType;
            try {
                interfaceType = Class.forName(metadataReader.getClassMetadata().getClassName());
                Config<Object> config = new Config<>();

                // Select handler according to different protocol
                String protocol = annotationAttributes.get("protocol").toString();
                config.put(new ConfigEntry<>("protocol", protocol));

                String name = annotationAttributes.get("value").toString();
                config.put(new ConfigEntry<>("name", name));

                String packageEof = getPackageEof(annotationAttributes.get("packageEof"), environment);
                config.put(new ConfigEntry<>("packageEof", packageEof));

                int packageMaxLength = getPackageMaxLength(annotationAttributes.get("packageMaxLength"), environment);
                config.put(new ConfigEntry<>("packageMaxLength", packageMaxLength));

                String discoveryDriverName = environment.getProperty("jsonrpc.discovery.driver-name");
                String discoveryUrl = environment.getProperty("jsonrpc.discovery.url");

                boolean hasDiscovery = StringUtils.hasLength(discoveryDriverName) && StringUtils.hasLength(discoveryUrl);

                JsonRpcClientInterface jsonRpcClient;

                String url = annotationAttributes.get("url").toString();
                /*
                 Use the client annotaion url value priorly.
                 If there is no url, check whether is there the discovery configuration, if not, throw exception.
                 */
                if (StringUtils.hasLength(url)) {
                    config.put(new ConfigEntry<>("url", url));
                    jsonRpcClient = getJsonRpcClient(protocol, config);
                } else {
                    Driver discovery = null;
                    if (hasDiscovery) {
                        JsonRpcServiceDiscovery jsonRpcServiceDiscovery = JsonRpcServiceDiscovery.newInstance(discoveryUrl, discoveryDriverName);
                        discovery = jsonRpcServiceDiscovery.getDriver();
                    }
                    if (discovery == null) {
                        log.error("The url of JsonRpcClient is required.");
                        return false;
                    }
                    config.put(new ConfigEntry<>("discovery", discovery));
                    jsonRpcClient = getJsonRpcClient(protocol, config);
                }

                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcClientFactoryBean.class);
                builder.addConstructorArgValue(interfaceType);
                builder.addPropertyValue("jsonRpcClient", jsonRpcClient);
                builder.addPropertyValue("service", name);
                beanDefinitionRegistry.registerBeanDefinition(interfaceType.getName(), builder.getBeanDefinition());
                return true;
            } catch (ClassNotFoundException e) {
                log.error("The interface of JsonRpcClient is not exists.");
                return false;
            }
        });
    }

    private JsonRpcClientInterface getJsonRpcClient(String protocol, Config<Object> config) throws IllegalArgumentException {
        protocol = protocol.toUpperCase();
        if (protocol.equals(JsonRpcProtocol.TCP.name())) {
            return new JsonRpcTcpClient(config);
        } else {
            return new JsonRpcHttpClient(config);
        }
    }

    private String getPackageEof(Object value, Environment environment) {
        String packageEof;
        if (!StringUtils.hasLength(value.toString())) {
            packageEof = environment.getProperty("jsonrpc.client.package-eof", RequestUtils.TCP_PACKAGE_EOF);
        } else {
            packageEof = value.toString();
        }
        return packageEof;
    }

    private int getPackageMaxLength(Object value, Environment environment) {
        int packageMaxLength = (int) value;
        if (packageMaxLength == 0) {
            packageMaxLength = environment.getProperty("jsonrpc.client.package-max-length", Integer.class, RequestUtils.TCP_PACKAG_MAX_LENGHT);
        }
        return packageMaxLength;
    }
}
