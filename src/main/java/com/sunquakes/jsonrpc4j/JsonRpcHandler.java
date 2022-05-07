package com.sunquakes.jsonrpc4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class JsonRpcHandler {

    public byte[] handler(String json) {
        try {
            Object typeObject = JSON.parse(json);
            if (typeObject instanceof JSONArray) {
                System.out.print("JSONArray");
            } else if (typeObject instanceof JSONObject) {
                System.out.print("JSONObject");
            } else {

            }
        } catch (JSONException e) {

        }
        return null;
    }

    public JSONObject handlerObject(JSONObject jsonObject) {
        return jsonObject;
    }

    public JSONArray handlerArray(JSONArray jsonArray) {
        return jsonArray;
    }
}
