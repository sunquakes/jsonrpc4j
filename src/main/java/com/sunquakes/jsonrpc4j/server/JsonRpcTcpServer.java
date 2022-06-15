package com.sunquakes.jsonrpc4j.server;

import com.sunquakes.jsonrpc4j.utils.RequestUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author : Robert, sunquakes@outlook.com
 * @version : 1.0.0
 * @since : 2022/5/21 1:32 PM
 **/
public class JsonRpcTcpServer extends JsonRpcServer implements InitializingBean {

    @Value("${jsonrpc.server.port}")
    private int port;

    @Value("${jsonrpc.server.pool.max-active:168}")
    private int poolMaxActive;

    private String packageEof;

    private int packageMaxLength;

    @Value("${jsonrpc.server.package-eof}")
    public void setPackageEof(String packageEof) {
        if (packageEof == null) packageEof = RequestUtils.TCP_PACKAGE_EOF;
        this.packageEof = packageEof;
    }

    @Value("${jsonrpc.server.package-max-length}")
    public void setPackageMaxLength(int packageMaxLength) {
        if (packageMaxLength == 0) packageMaxLength = RequestUtils.TCP_PACKAG_MAX_LENGHT;
        this.packageMaxLength = packageMaxLength;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public void start() {
        Thread t = new ServerThread(applicationContext, port, new TcpServerOption(packageEof, packageMaxLength, new TcpServerPoolOption(poolMaxActive)));
        t.start();
    }
}

class ServerThread extends Thread {

    private ApplicationContext applicationContext;

    private int port;

    ExecutorService pool;

    private TcpServerOption tcpServerOption;

    ServerThread(ApplicationContext applicationContext, int port, TcpServerOption tcpServerOption) {
        this.applicationContext = applicationContext;
        this.port = port;
        this.tcpServerOption = tcpServerOption;
        this.pool = Executors.newFixedThreadPool(tcpServerOption.getPoolOption().getMaxActive());
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                pool.execute(new JsonRpcTcpServerHandler(applicationContext, socket, tcpServerOption));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
