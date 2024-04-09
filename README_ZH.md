[English](README.md) | 🇨🇳中文

# jsonrpc4j

<p align="center"><a href="https://moonquakes.io/" target="_blank" rel="noopener noreferrer"><img width="200" src="https://www.moonquakes.io/images/logo.png" alt="moonquakes logo"></a></p>
<p align="center">
    <img alt="jdk" src="https://img.shields.io/badge/jdk-%3E%3D17-red">
    <a href="https://github.com/sunquakes/jsonrpc4j"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.sunquakes/jsonrpc4j"></a>
    <img alt="GitHub" src="https://img.shields.io/github/license/sunquakes/jsonrpc4j?color=blue">
</p> 

## 安装

- pom.xml文件中添加snapshots源

```xml

<repositories>
    <repository>
        <id>releases</id>
        <name>Releases</name>
        <url>https://oss.sonatype.org/content/repositories/releases/</url>
    </repository>
    <repository>
        <id>snapshots</id>
        <name>Snapshots</name>
        <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    </repository>
</repositories>
```

- pom.xml文件中添加依赖

```xml
<!-- https://mvnrepository.com/artifact/com.sunquakes/jsonrpc4j -->
<dependency>
    <groupId>com.sunquakes</groupId>
    <artifactId>jsonrpc4j</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

## 开始使用

### 步骤1. 用@JsonRpcScan注解配置basePackages，扫描Service和Client

- 方法1. 用配置文件方式注入

```java

@Configuration
@JsonRpcScan({"com.sunquakes"})
public class JsonRpcConfig {
}
```

- 方法2. 在入口Application文件中注入

```java

@SpringBootApplication
@JsonRpcScan({"com.sunquakes.jsonrpc4j.spring.boot"})
public class JsonRpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(JsonRpcApplication.class, args);
    }
}
```

### 步骤2. 配置服务端

- 添加服务端配置项到application.properties文件中

```properties
# 配置服务端采用的协议，可以配置tcp或http。
jsonrpc.server.protocol=tcp
# 配置服务端监听的端口。
jsonrpc.server.port=3200
# 如果采用的协议是http，接下来的两个配置项可以忽略。
# 单次数据的结束符，分隔服务端接收的数据。
jsonrpc.server.package-eof=\r\n
# 服务端接收数据的缓存区大小。
jsonrpc.server.package-max-length=2097152
```

### 步骤3. 创建服务接口文件和实现类文件，实现业务逻辑

- 创建一个名为IJsonRpcService的接口文件，添加注解@JsonRpcService，value值为服务名

```java

@JsonRpcService(value = "JsonRpc")
public interface IJsonRpcService {
    int add(int a, int b);
}
```

- 创建一个名为JsonRpcServiceImpl的类文件去实现接口IJsonRpcService

```java
public class JsonRpcServiceImpl implements IJsonRpcService {
    @Override
    public Integer add(int a, int b) {
        return a + b;
    }
}
```

### 步骤4. 配置客户端

- 添加客户端配置项到application.properties文件中

```properties
# Symbol for the end of data request once.
jsonrpc.client.package-eof=\r\n
```

### 步骤5. 创建客户端接口文件

-

创建一个名为IJsonRpcClient的接口文件，添加注解@JsonRpcClient，其中配置的value值为请求的服务名（与步骤3的value值对应），protocol值可以是tcp/http/https（与步骤2中配置的jsonrpc.server.protocol值对应）。

```java

@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.TCP, url = "localhost:3200")
public interface IJsonRpcClient {
    int add(int a, int b);
}
```

### 步骤6. 用客户端请求服务端

```java
public class JsonRpcTest {

    @Autowired
    private IJsonRpcClient jsonRpcClient;

    public int add() {
        // 发送的数据格式: {"id":"691bbf41-e992-4f45-9c0d-1eeb0b088698","jsonrpc":"2.0","method":"json_rpc/add","params":{"a":3,"b":4}}
        // 接收的数据格式: {"id":"691bbf41-e992-4f45-9c0d-1eeb0b088698","jsonrpc":"2.0","result":7}
        return jsonRpcClient.add(1, 2); // 返回结果是3
    }
}
```

## 更多特性

### 服务发现

- Consul

```properties
# 主机名，非必填项，如果用默认主机ip，此项可以不配置
jsonrpc.discovery.hostname=192.168.39.1
# consul地址
# instanceId: 用来区分不同节点上拥有相同主机名和端口的 
# check: 开启健康检查，默认关闭
# checkInterval: 健康检查周期，默认60s
jsonrpc.discovery.url=http://127.0.0.1:8500?instanceId=2&check=true&checkInterval=5s
# consul驱动
jsonrpc.discovery.driver-name=com.sunquakes.jsonrpc4j.discovery.Consul
```

- Nacos

```properties
# 主机名，非必填项，如果用默认主机ip，此项可以不配置
jsonrpc.discovery.hostname=192.168.39.1
# nacos地址
# 支持 [Open API Guide](https://nacos.io/zh-cn/docs/open-api.html) 注册实例章节的请求参数
jsonrpc.discovery.url=http://127.0.0.1:8849?namespaceId=XXXXXX&...
# consul驱动
jsonrpc.discovery.driver-name=com.sunquakes.jsonrpc4j.discovery.Nacos
```

## 测试

```shell
mvn test
```



