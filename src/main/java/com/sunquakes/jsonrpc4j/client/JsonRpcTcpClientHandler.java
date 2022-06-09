package com.sunquakes.jsonrpc4j.client;

import com.alibaba.fastjson2.JSONObject;
import com.sunquakes.jsonrpc4j.dto.ResponseDto;
import com.sunquakes.jsonrpc4j.utils.ByteArrayUtils;
import com.sunquakes.jsonrpc4j.utils.RequestUtils;
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

    private String packageEof = RequestUtils.TCP_PACKAGE_EOF;

    private int packageMaxLength = RequestUtils.TCP_PACKAG_MAX_LENGHT;

    private String url;

    private byte[] initBytes = new byte[0];

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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(initBytes);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        byte[] buffer = new byte[packageMaxLength];
        int bufferLength = buffer.length;
        int len;
        InputStream is = s.getInputStream();

        String packageEof = "\r\n";
        int packageEofLength = packageEof.length();

        byte[] packageEofBytes = packageEof.getBytes();
        int packageEofBytesLength = packageEofBytes.length;

        int i = ByteArrayUtils.strstr(bytes, packageEofBytes);
        if (i != -1) {
            if (i + packageEofBytesLength < bytes.length) {
                initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
            } else {
                initBytes = new byte[0];
            }
            bytes = Arrays.copyOfRange(bytes, 0, i);
        } else {
            while ((len = is.read(buffer)) != -1) {
                if (bufferLength == len) {
                    byteArrayOutputStream.write(buffer);
                } else {
                    byte[] end = Arrays.copyOfRange(buffer, 0, len);
                    byteArrayOutputStream.write(end);
                }
                bytes = byteArrayOutputStream.toByteArray();
                i = ByteArrayUtils.strstr(bytes, packageEofBytes);
                if (i != -1) {
                    if (i + packageEofBytesLength < bytes.length) {
                        initBytes = Arrays.copyOfRange(bytes, i + packageEofBytesLength, bytes.length);
                    } else {
                        initBytes = new byte[0];
                    }
                    bytes = Arrays.copyOfRange(bytes, 0, i);
                    break;
                }
            }
        }
        ResponseDto responseDto = JSONObject.parseObject(new String(bytes), ResponseDto.class);
        return responseDto.getResult();
    }

    public JsonRpcTcpClientHandler setPackageEof(String packageEof) {
        this.packageEof = packageEof;
        return this;
    }

    public JsonRpcTcpClientHandler setPackageMaxLength(int packageMaxLength) {
        this.packageMaxLength = packageMaxLength;
        return this;
    }
}
