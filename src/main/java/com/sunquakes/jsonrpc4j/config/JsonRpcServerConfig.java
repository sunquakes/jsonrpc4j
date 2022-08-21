package com.sunquakes.jsonrpc4j.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 2.0.2
 * @since : 2022/8/21 10:53 AM
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jsonrpc.server")
public class JsonRpcServerConfig {

    private String protocol;

    private int port;

    @Data
    public static class Ssl {

        private String keyStore;

        private String keyStoreType;

        private String keyStorePassword;
    }

    private Ssl ssl;

    @Data
    public static class Pool {

        private int maxActive;
    }

    private Pool pool;

    @Data
    public static class Netty {

        @Data
        public static class Group {

            private int threadNum;
        }

        private Group bossGroup;

        private Group workerGroup;
    }

    private Netty netty;
}
