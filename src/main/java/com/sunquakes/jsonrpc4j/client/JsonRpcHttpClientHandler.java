package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.client
 * @Author: Robert
 * @CreateTime: 2022/5/21 1:32 PM
 **/
public class JsonRpcHttpClientHandler implements JsonRpcClientHandlerInterface {

    @Override
    public Object handle(String method, Object[] args) throws IOException {
        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", "JsonRpc/add");
        request.put("params", args);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://localhost:3200");
        httpPost.setEntity(new StringEntity(request.toString(), ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        String json = EntityUtils.toString(response.getEntity());
        ResponseDto responseDto = JSONObject.parseObject(json, ResponseDto.class);
        return responseDto.getResult();
    }
}
