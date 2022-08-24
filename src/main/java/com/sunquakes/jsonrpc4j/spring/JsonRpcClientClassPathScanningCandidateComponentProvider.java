package com.sunquakes.jsonrpc4j.spring;

import com.sunquakes.jsonrpc4j.client.*;
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

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since: 2022/6/6 9:17 PM
 **/
@Slf4j
public class JsonRpcClientClassPathScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {

    public JsonRpcClientClassPathScanningCandidateComponentProvider(boolean useDefaultFilters, Environment environment, BeanDefinitionRegistry beanDefinitionRegistry) {
        super(useDefaultFilters);
        super.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                Map<String, Object> annotationAttributes = metadataReader
                        .getAnnotationMetadata()
                        .getAnnotationAttributes("com.sunquakes.jsonrpc4j.JsonRpcClient");

                if (annotationAttributes == null)
                    return false;

                Class<?> interfaceType = null;
                try {
                    interfaceType = Class.forName(metadataReader.getClassMetadata().getClassName());

                    // Select handler according to different protocol
                    String protocol = annotationAttributes.get("protocol").toString();
                    String url = annotationAttributes.get("url").toString();
                    if (!StringUtils.hasLength(url)) {
                        log.error("The url of JsonRpcClient is required.");
                        return false;
                    }

                    String packageEof = annotationAttributes.get("packageEof").toString();
                    if (!StringUtils.hasLength(packageEof)) {
                        packageEof = environment.getProperty("jsonrpc.client.package-eof", RequestUtils.TCP_PACKAGE_EOF);
                    }
                    int packageMaxLength = (int) annotationAttributes.get("packageMaxLength");
                    if (packageMaxLength == 0) {
                        packageMaxLength = Integer.valueOf(environment.getProperty("jsonrpc.client.package-max-length", String.valueOf(RequestUtils.TCP_PACKAG_MAX_LENGHT)));
                    }
                    JsonRpcClientInterface jsonRpcClient = getJsonRpcClient(protocol, url, packageEof, packageMaxLength);

                    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JsonRpcClientFactoryBean.class);
                    builder.addConstructorArgValue(interfaceType);
                    builder.addPropertyValue("jsonRpcClient", jsonRpcClient);
                    builder.addPropertyValue("service", annotationAttributes.get("value"));
                    beanDefinitionRegistry.registerBeanDefinition(interfaceType.getName(), builder.getBeanDefinition());
                    return true;
                } catch (ClassNotFoundException e) {
                    log.error("The interface of JsonRpcClient is not exists.");
                    return false;
                }
            }
        });
    }

    private JsonRpcClientInterface getJsonRpcClient(String protocol, String url, String packageEof, int packageMaxLength) throws IllegalArgumentException {
        if (protocol.equals(RequestUtils.PROTOCOL_TCP)) {
            return new JsonRpcTcpClient(url, new TcpClientOption(packageEof, packageMaxLength));
        } else {
            return new JsonRpcHttpClient(protocol, url);
        }
    }
}
