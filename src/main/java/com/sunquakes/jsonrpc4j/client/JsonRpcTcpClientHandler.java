package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import lombok.Synchronized;
import org.apache.commons.pool2.ObjectPool;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * @Project: jsonrpc4j
 * @Package: com.sunquakes.jsonrpc4j.client
 * @Author: Robert
 * @CreateTime: 2022/5/24 12:43 PM
 **/
public class JsonRpcTcpClientHandler implements JsonRpcClientHandlerInterface {

    private String packageEof = "\r\n";

    private String url;

    private String init = "";

    public JsonRpcTcpClientHandler(String url) {
        this.url = url;
    }

    @Override
    public Object handle(String method, Object[] args) throws Exception {
        JSONObject request = new JSONObject();
        request.put("id", RequestUtils.getId());
        request.put("jsonrpc", RequestUtils.JSONRPC);
        request.put("method", method);
        request.put("params", args);

        ObjectPool<Socket> socketPool = SocketPoolFactory.getPool(url);
        Socket s = socketPool.borrowObject();

        OutputStream os = s.getOutputStream();
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        bw.write((request + packageEof));
        bw.flush();

        StringBuffer sb = new StringBuffer(init);

        byte[] buffer = new byte[10];
        int bufferLength = buffer.length;
        int len;
        InputStream is = s.getInputStream();

        String packageEof = "\r\n";
        int packageEofLength = packageEof.length();

        int i = sb.indexOf(packageEof);
        if (i != -1) {
            sb.substring(0, i);
            if (i + packageEofLength < sb.length()) {
                init = sb.substring(i + packageEofLength);
            } else {
                init = "";
            }
        } else {
            while ((len = is.read(buffer)) != -1) {
                if (bufferLength == len) {
                    sb.append(new String(buffer));
                } else {
                    byte[] end = Arrays.copyOfRange(buffer, 0, len);
                    sb.append(new String(end));
                }
                i = sb.indexOf(packageEof);
                if (i != -1) {
                    sb.substring(0, i);
                    if (i + packageEofLength < sb.length()) {
                        init = sb.substring(i + packageEofLength);
                    } else {
                        init = "";
                    }
                    break;
                }
            }
        }
        ResponseDto responseDto = JSONObject.parseObject(sb.toString(), ResponseDto.class);
        return responseDto.getResult();
    }
}
