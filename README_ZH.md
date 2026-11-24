[English](README.md) | 🇨🇳中文

# jsonrpc4j

<p align="center"><a href="https://moonquakes.io/" target="_blank" rel="noopener noreferrer"><img width="200" src="https://www.moonquakes.io/images/logo.png" alt="moonquakes logo"></a></p>
<p align="center">
    <img alt="jdk" src="https://img.shields.io/badge/jdk-%3E%3D17-red">
    <a href="https://github.com/sunquakes/jsonrpc4j"><img alt="Maven Central" src="https://img.shields.io/maven-central/v/com.sunquakes/jsonrpc4j"></a>
    <img alt="GitHub" src="https://img.shields.io/github/license/sunquakes/jsonrpc4j?color=blue">
</p> 

## 安装

- pom.xml文件中添加依赖

```xml
<!-- https://mvnrepository.com/artifact/com.sunquakes/jsonrpc4j -->
<dependency>
    <groupId>com.sunquakes</groupId>
    <artifactId>jsonrpc4j</artifactId>
    <version>3.0.0</version>
</dependency>
```

- 最新正式版：[Maven Central 上的 3.0.0](https://central.sonatype.com/artifact/com.sunquakes/jsonrpc4j/3.0.0)

正式版直接从Maven Central获取，无需配置仓库。

- 使用快照版本时，在pom.xml文件中添加Sonatype Central快照仓库

```xml

<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <name>Sonatype Central Snapshots</name>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    </repository>
</repositories>
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
# 配置服务端采用的协议，可以配置tcp、http、https或streamable-http。
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

创建一个名为IJsonRpcClient的接口文件，添加注解@JsonRpcClient，其中配置的value值为请求的服务名（与步骤3的value值对应），protocol值可以是tcp/http/https/streamable-http（与步骤2中配置的jsonrpc.server.protocol值对应）。

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

### Streamable HTTP（兼容 MCP）

配置 `jsonrpc.server.protocol=streamable-http` 后，所有 `@JsonRpcService` 都会发布到单个 `/mcp` 端点，实现 MCP
Streamable HTTP 传输协议。

```properties
jsonrpc.server.protocol=streamable-http
jsonrpc.server.port=3200
```

- `POST /mcp` 在单个结果时返回 `application/json`；当服务方法返回数组、`Iterable`、`Iterator` 或 `Stream` 时返回
  `text/event-stream`，逐个元素推送 JSON-RPC 响应。
- 通过 `Mcp-Session-Id` 维护会话，通过 `Mcp-Protocol-Version` 协商协议版本；`GET /mcp` 打开 SSE 通道，支持
  `Last-Event-ID` 重放与 keepalive 注释。
- 通知请求返回 `202 Accepted`，`DELETE /mcp` 关闭会话并返回 `204 No Content`。
- `Origin` 与 `Host` 不一致的请求返回 `403 Forbidden`。
- 注册的服务会自动发布为名为 `Service_method` 的 MCP 工具，并根据 Java 参数名反射生成输入 schema，MCP agent 可调用
  `initialize`、`ping`、`tools/list` 和 `tools/call`。传入 `_meta.progressToken` 时，流式工具结果会转换为
  `notifications/progress` 事件。旧有的 `Service/method` 分发在同一端点继续可用。

```java
Config<Object> config = new Config<>();
config.put(new ConfigEntry<>("protocol", "streamable-http"));
config.put(new ConfigEntry<>("name", "JsonRpc"));
config.put(new ConfigEntry<>("url", "localhost:3200"));
JsonRpcStreamableHttpClient client = new JsonRpcStreamableHttpClient(config);
Object sum = client.call("add", 1, 2); // 一元结果，兼容 JSON 与 SSE
try (Stream<Object> values = client.stream("numbers", 3)) {
    values.forEach(System.out::println); // 每个 SSE 事件一个值
}
client.close(); // 关闭会话
```

也可以通过接口声明：
`@JsonRpcClient(value = "JsonRpc", protocol = JsonRpcProtocol.STREAMABLE_HTTP, url = "localhost:3200")`。

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



