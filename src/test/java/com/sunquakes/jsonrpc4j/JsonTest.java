package com.sunquakes.jsonrpc4j;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.discovery.Nacos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class JsonTest {

    @Test
    void testUnescape() {
        String json = "{\"name\":\"DEFAULT_GROUP@@php_http\",\"groupName\":\"DEFAULT_GROUP\",\"clusters\":\"\",\"cacheMillis\":10000,\"hosts\":[{\"instanceId\":\"10.222.1.164#9504#DEFAULT#DEFAULT_GROUP@@php_http\",\"ip\":\"10.222.1.164\",\"port\":9504,\"weight\":1.0,\"healthy\":true,\"enabled\":true,\"ephemeral\":false,\"clusterName\":\"DEFAULT\",\"serviceName\":\"DEFAULT_GROUP@@php_http\",\"metadata\":{\"server\":\"jsonrpc-http\",\"protocol\":\"jsonrpc-http\",\"className\":\"App\\\\JsonRpc\\\\PhpHttpService\",\"publishTo\":\"nacos\"},\"ipDeleteTimeout\":30000,\"instanceHeartBeatInterval\":5000,\"instanceHeartBeatTimeOut\":15000}],\"lastRefTime\":1675518467553,\"checksum\":\"\",\"allIPs\":false,\"reachProtectionThreshold\":false,\"valid\":true}";
        Nacos.GetResp resp = JSONObject.parseObject(json, Nacos.GetResp.class);
        assertNotNull(resp);
    }
}
