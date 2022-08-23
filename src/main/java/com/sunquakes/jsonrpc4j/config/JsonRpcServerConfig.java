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

    public enum Protocol {
        HTTP,
        HTTPS,
        TCP;
    }

    private Protocol protocol;

    private int port = 3200;

    private String packageEof = "\\r\\n";

    private int packageMaxLength = 4096;

    @Data
    public static class Ssl {

        private String keyStore;

        private String keyStoreType;

        private String keyStorePassword;
    }

    private Ssl ssl;

    @Data
    public static class Pool {

        private int maxActive = 168;
    }

    private Pool pool;

    @Data
    public static class Netty {

        @Data
        public static class BossGroup {

            private int threadNum = 1;
        }

        private BossGroup bossGroup;

        @Data
        public static class WorkerGroup {

            private int threadNum = 0;
        }

        private WorkerGroup workerGroup;
    }

    private Netty netty;
}
