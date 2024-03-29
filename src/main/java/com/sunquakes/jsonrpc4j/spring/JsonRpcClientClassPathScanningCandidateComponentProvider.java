package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.*;
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
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * @author : Shing, sunquakes@outlook.com
 * @version : 1.0.0
 * @since: 2022/6/6 9:17 PM
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
                Config config = new Config();

                // Select handler according to different protocol
                String protocol = annotationAttributes.get("protocol").toString();
                config.put(new ConfigEntry("protocol", protocol));

                String name = annotationAttributes.get("value").toString();
                config.put(new ConfigEntry("name", name));

                String packageEof = annotationAttributes.get("packageEof").toString();
                if (!StringUtils.hasLength(packageEof)) {
                    packageEof = environment.getProperty("jsonrpc.client.package-eof", RequestUtils.TCP_PACKAGE_EOF);
                }
                config.put(new ConfigEntry("packageEof", packageEof));

                int packageMaxLength = (int) annotationAttributes.get("packageMaxLength");
                if (packageMaxLength == 0) {
                    packageMaxLength = environment.getProperty("jsonrpc.client.package-max-length", Integer.class, RequestUtils.TCP_PACKAG_MAX_LENGHT);
                }
                config.put(new ConfigEntry("packageMaxLength", packageMaxLength));

                String discoveryDriverName = environment.getProperty("jsonrpc.discovery.driver-name");
                String discoveryUrl = environment.getProperty("jsonrpc.discovery.url");

                boolean hasDiscovery = StringUtils.hasLength(discoveryDriverName) && StringUtils.hasLength(discoveryUrl);

                JsonRpcClientInterface jsonRpcClient;

                String url = annotationAttributes.get("url").toString();
                /**
                 * Use the client annotaion url value priorly.
                 * If there is no url, check whether is there the discovery configuration, if not, throw exception.
                 */
                if (StringUtils.hasLength(url)) {
                    config.put(new ConfigEntry("url", url));
                    jsonRpcClient = getJsonRpcClient(protocol, config);
                } else {
                    Driver discovery = null;
                    if (hasDiscovery) {
                        JsonRpcServiceDiscovery jsonRpcServiceDiscovery = JsonRpcServiceDiscovery.newInstance(discoveryUrl, discoveryDriverName);
                        discovery = jsonRpcServiceDiscovery.getDriver();
                    }
                    if (discovery != null) {
                        config.put(new ConfigEntry("discovery", discovery));
                        jsonRpcClient = getJsonRpcClient(protocol, config);
                    } else {
                        log.error("The url of JsonRpcClient is required.");
                        return false;
                    }
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

    private JsonRpcClientInterface getJsonRpcClient(String protocol, Config config) throws IllegalArgumentException {
        if (protocol.equals(RequestUtils.PROTOCOL_TCP)) {
            return new JsonRpcTcpClient(config);
        } else {
            return new JsonRpcHttpClient(config);
        }
    }
}
