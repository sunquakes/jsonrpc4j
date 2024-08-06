package com.sunquakes.jsonrpc4j.spring;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author Shing Rui {@link "mailto:sunquakes@outlook.com"}
 * @version 1.0.0
 * @since 1.0.0
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({JsonRpcImportBeanDefinitionRegistrar.class})
public @interface JsonRpcScan {

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     *
     * @return base packages
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * Base packages to scan for annotated jsonrpc4j components.
     *
     * @return base packages
     */
    @AliasFor("value")
    String[] basePackages() default {};
}
